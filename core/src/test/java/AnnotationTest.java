import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.pl3x.livemap.LiveMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;
import static org.junit.jupiter.api.Assertions.fail;

// Test borrowed from Bukkit API
// https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/test/java/org/bukkit/AnnotationTest.java

public class AnnotationTest {
    private static final String[] ACCEPTED_ANNOTATIONS = {
        "Lorg/jetbrains/annotations/Nullable;",
        "Lorg/jetbrains/annotations/NotNull;",
        "Lnet/pl3x/livemap/util/Unsafe$UnknownNullability;"
    };

    private static final String[] EXCLUDED_CLASSES = {
        ""
    };

    @Test
    public void testAll() throws IOException, URISyntaxException {
        URL loc = LiveMap.class.getProtectionDomain().getCodeSource().getLocation();
        File file = new File(loc.toURI());

        final HashMap<String, ClassNode> foundClasses = new HashMap<>();
        collectClasses(file, foundClasses);

        final ArrayList<String> errors = new ArrayList<>();

        for (ClassNode clazz : foundClasses.values()) {
            if (!isClassIncluded(clazz, foundClasses)) {
                continue;
            }

            for (MethodNode method : clazz.methods) {
                if (!isMethodIncluded(clazz, method, foundClasses)) {
                    continue;
                }

                if (mustBeAnnotated(Type.getReturnType(method.desc)) && !isWellAnnotated(method.invisibleAnnotations)) {
                    // Paper start - Allow use of TYPE_USE annotations
                    boolean warn = true;
                    if (isWellAnnotated(method.visibleTypeAnnotations)) {
                        warn = false;
                    } else if (method.invisibleTypeAnnotations != null) {
                        for (final org.objectweb.asm.tree.TypeAnnotationNode invisibleTypeAnnotation : method.invisibleTypeAnnotations) {
                            final org.objectweb.asm.TypeReference ref = new org.objectweb.asm.TypeReference(invisibleTypeAnnotation.typeRef);
                            if (ref.getSort() == org.objectweb.asm.TypeReference.METHOD_RETURN && java.util.Arrays.asList(ACCEPTED_ANNOTATIONS).contains(invisibleTypeAnnotation.desc)) {
                                warn = false;
                                break;
                            }
                        }
                    }
                    if (warn)
                        // Paper end
                        warn(errors, clazz, method, "return value");
                }

                Type[] paramTypes = Type.getArgumentTypes(method.desc);
                List<ParameterNode> parameters = method.parameters;

                dancing:
                // Paper
                for (int i = 0; i < paramTypes.length; i++) {
                    if (mustBeAnnotated(paramTypes[i]) ^ isWellAnnotated(method.invisibleParameterAnnotations == null ? null : method.invisibleParameterAnnotations[i])) {
                        // Paper start
                        if (method.invisibleTypeAnnotations != null) {
                            for (final org.objectweb.asm.tree.TypeAnnotationNode invisibleTypeAnnotation : method.invisibleTypeAnnotations) {
                                final org.objectweb.asm.TypeReference ref = new org.objectweb.asm.TypeReference(invisibleTypeAnnotation.typeRef);
                                if (ref.getSort() == org.objectweb.asm.TypeReference.METHOD_FORMAL_PARAMETER && ref.getTypeParameterIndex() == i && java.util.Arrays.asList(ACCEPTED_ANNOTATIONS).contains(invisibleTypeAnnotation.desc)) {
                                    continue dancing;
                                }
                            }
                        }
                        if (method.visibleTypeAnnotations != null) {
                            for (final org.objectweb.asm.tree.TypeAnnotationNode invisibleTypeAnnotation : method.visibleTypeAnnotations) {
                                final org.objectweb.asm.TypeReference ref = new org.objectweb.asm.TypeReference(invisibleTypeAnnotation.typeRef);
                                if (ref.getSort() == org.objectweb.asm.TypeReference.METHOD_FORMAL_PARAMETER && ref.getTypeParameterIndex() == i && java.util.Arrays.asList(ACCEPTED_ANNOTATIONS).contains(invisibleTypeAnnotation.desc)) {
                                    continue dancing;
                                }
                            }
                        }
                        // Paper end - Allow use of TYPE_USE annotations
                        ParameterNode paramNode = parameters == null ? null : parameters.get(i);
                        String paramName = paramNode == null ? null : paramNode.name;

                        warn(errors, clazz, method, "parameter " + i + (paramName == null ? "" : ": " + paramName));
                    }
                }
            }
        }

        if (errors.isEmpty()) {
            // Success
            return;
        }

        Collections.sort(errors);

        StringBuilder builder = new StringBuilder()
            .append("\nThere ")
            .append(errors.size() != 1 ? "are " : "is ")
            .append(errors.size())
            .append(" missing annotation")
            .append(errors.size() != 1 ? "s:\n" : ":\n");

        for (String message : errors) {
            builder.append("    ").append(message).append("\n");
        }

        System.err.print(builder);
        fail(builder.insert(0, "\n").toString());
    }

    private static void collectClasses(@NotNull File from, @NotNull Map<String, ClassNode> to) throws IOException {
        if (from.isDirectory()) {
            final File[] files = from.listFiles();
            assert files != null;

            for (File file : files) {
                collectClasses(file, to);
            }
            return;
        }

        if (!from.getName().endsWith(".class")) {
            return;
        }

        try (FileInputStream in = new FileInputStream(from)) {
            final ClassReader cr = new ClassReader(in);

            final ClassNode node = new ClassNode();
            cr.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            to.put(node.name, node);
        }
    }

    private static boolean isClassIncluded(@NotNull ClassNode clazz, @NotNull Map<String, ClassNode> allClasses) {
        // Exclude synthetic or deprecated classes and annotations, since their members can't be null
        if ((clazz.access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_DEPRECATED | Opcodes.ACC_ANNOTATION)) != 0) {
            return false;
        }

        if (isSubclassOf(clazz, "org/bukkit/material/MaterialData", allClasses)) {
            throw new AssertionError("Subclass of MaterialData must be deprecated: " + clazz.name);
        }

        if (isSubclassOf(clazz, "java/lang/Exception", allClasses)
            || isSubclassOf(clazz, "java/lang/RuntimeException", allClasses)) {
            // Exceptions are excluded
            return false;
        }
        // Paper start
        if (isInternal(clazz.invisibleAnnotations)) {
            return false;
        }
        // Paper end

        for (String excludedClass : EXCLUDED_CLASSES) {
            if (excludedClass.equals(clazz.name)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isMethodIncluded(@NotNull ClassNode clazz, @NotNull MethodNode method, @NotNull Map<String, ClassNode> allClasses) {
        // Exclude synthetic and deprecated methods
        if ((method.access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_DEPRECATED)) != 0) {
            return false;
        }

        // Exclude Java methods
        if (is(method, "toString", 0) || is(method, "clone", 0) || is(method, "equals", 1)) {
            return false;
        }

        // Exclude generated Enum methods
        if (isSubclassOf(clazz, "java/lang/Enum", allClasses) && (is(method, "values", 0) || is(method, "valueOf", 1))) {
            return false;
        }

        // Exclude synthetic enum constructors
        if (isEnumConstructor(clazz, method)) {
            return false;
        }

        // Paper start
        if (isInternal(method.invisibleAnnotations)) {
            return false;
        }
        // Paper end

        // Anonymous classes have generated constructors, which can't be annotated nor invoked
        return !"<init>".equals(method.name) || !isAnonymous(clazz);
    }

    private static boolean isEnumConstructor(@NotNull ClassNode clazz, @NotNull MethodNode method) {
        // Must be a class that is an enum
        if ((clazz.access & Opcodes.ACC_ENUM) == 0) {
            return false;
        }

        // Must be a constructor name `<init>`
        if (!"<init>".equals(method.name)) {
            return false;
        }

        // Parse argument types from the method descriptor
        Type[] argTypes = Type.getArgumentTypes(method.desc);

        // Enum constructors always start with (String, int) for name and ordinal
        return argTypes.length >= 2 &&
            argTypes[0].equals(Type.getObjectType("java/lang/String")) &&
            argTypes[1].equals(Type.INT_TYPE);
    }

    // Paper start
    private static boolean isInternal(List<? extends AnnotationNode> annotations) {
        if (annotations == null) {
            return false;
        }
        for (AnnotationNode node : annotations) {
            if (node.desc.equals("Lorg/jetbrains/annotations/ApiStatus$Internal;")) {
                return true;
            }
        }

        return false;
    }
    // Paper end

    private static boolean isWellAnnotated(@Nullable List<? extends AnnotationNode> annotations) { // Paper
        if (annotations == null) {
            return false;
        }

        for (AnnotationNode node : annotations) {
            for (String acceptedAnnotation : ACCEPTED_ANNOTATIONS) {
                if (acceptedAnnotation.equals(node.desc)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean mustBeAnnotated(@NotNull Type type) {
        int sort = type.getSort();
        if (sort == Type.ARRAY) {
            sort = type.getElementType().getSort();
        }
        return sort == Type.OBJECT;
    }

    private static boolean is(@NotNull MethodNode method, @NotNull String name, int parameters) {
        final List<ParameterNode> params = method.parameters;
        return method.name.equals(name) && (params == null || params.size() == parameters);
    }

    /**
     * Checks if the class is anonymous.
     *
     * @param clazz the class to check
     * @return true if given class is anonymous
     */
    private static boolean isAnonymous(@NotNull ClassNode clazz) {
        final String name = clazz.name;
        if (name == null) {
            return false;
        }
        final int nestedSeparator = name.lastIndexOf('$');
        if (nestedSeparator == -1 || nestedSeparator + 1 == name.length()) {
            return false;
        }

        // Nested classes have purely numeric names. Java classes can't begin with a number,
        // so if first character is a number, the class must be anonymous
        final char c = name.charAt(nestedSeparator + 1);
        return c >= '0' && c <= '9';
    }

    private static boolean isSubclassOf(@NotNull ClassNode what, @NotNull String ofWhat, @NotNull Map<String, ClassNode> allClasses) {
        if (ofWhat.equals(what.name)
            // Not only optimization: Super class may not be present in allClasses, so it is checked here
            || ofWhat.equals(what.superName)) {
            return true;
        }

        final ClassNode parent = allClasses.get(what.superName);
        if (parent != null && isSubclassOf(parent, ofWhat, allClasses)) {
            return true;
        }

        for (String superInterface : what.interfaces) {
            final ClassNode interfaceParent = allClasses.get(superInterface);
            if (interfaceParent != null && isSubclassOf(interfaceParent, ofWhat, allClasses)) {
                return true;
            }
        }

        return false;
    }

    private static void warn(@NotNull Collection<String> out, @NotNull ClassNode clazz, @NotNull MethodNode method, @NotNull String description) {
        out.add(clazz.name + " \t" + method.name + " \t" + description);
    }
}
