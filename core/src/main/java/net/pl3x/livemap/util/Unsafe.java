package net.pl3x.livemap.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NonNls;

/**
 * Unsafe utils.
 */
public final class Unsafe {
    private Unsafe() {
    }

    /**
     * Cast object to T.
     *
     * @param obj Object to cast
     * @param <T> Type to cast to
     * @return Object as T
     */
    @UnknownNullability(value = "Unsafe cast. Could be null.")
    @SuppressWarnings("unchecked")
    public static <T> T cast(@UnknownNullability Object obj) {
        return (T) obj;
    }

    /**
     * An element annotated with {@code UnknownNullability} claims that no specific nullability
     * should be assumed by static analyzer. The unconditional dereference of the annotated value
     * should not trigger a static analysis warning by default (though static analysis tool may have
     * an option to perform stricter analysis and issue warnings for {@code @UnknownNullability} as well).
     * It's mainly useful at method return types to mark methods that may occasionally
     * return {@code null} but in many cases, user knows that in this particular code path
     * {@code null} is not possible, so producing a warning would be annoying.
     */
    @Documented
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
    public @interface UnknownNullability {
        /**
         * Human-readable description of the circumstances, in which the type is nullable.
         *
         * @return textual reason when the annotated value could be null, for documentation purposes
         */
        @NonNls String value() default "";
    }
}
