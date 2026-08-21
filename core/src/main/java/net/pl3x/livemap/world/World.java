package net.pl3x.livemap.world;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.configuration.Lang;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a renderable world.
 */
public abstract class World {
    private final String name;

    private final Path regionDir;
    private final Path tilesDir;

    /**
     * Constructs a new instance of World.
     *
     * @param name       Name of world
     * @param regionsDir Regions directory
     */
    public World(@NotNull String name, @NotNull Path regionsDir) {
        this.name = name;
        this.regionDir = regionsDir;
        this.tilesDir = LiveMap.api().getTilesDir().resolve(name.replace(":", "-"));
    }

    /**
     * Get this world's platform specific level.
     *
     * @param <T> Platform specific level type
     * @return This world's platform specific level
     */
    @NotNull
    public abstract <T> T getLevel();

    /**
     * Get the name of this world.
     *
     * @return World's name
     */
    @NotNull
    public String getName() {
        return this.name;
    }

    /**
     * Get path to regions directory.
     *
     * @return Regions directory
     */
    @NotNull
    public Path getRegionsDir() {
        return this.regionDir;
    }

    /**
     * Get path to tiles directory.
     *
     * @return Tiles directory
     */
    @NotNull
    public Path getTilesDir() {
        return this.tilesDir;
    }

    /**
     * Represents a custom command argument for our world type.
     */
    public static class Argument implements ArgumentType<World> {
        /**
         * Error for when a specified world name/id is invalid.
         */
        public static final SimpleCommandExceptionType ERROR_INVALID_WORLD = new SimpleCommandExceptionType(() -> Lang.ERROR_INVALID_WORLD);
        /**
         * Error for when a specified world is not found.
         */
        public static final SimpleCommandExceptionType ERROR_MISSING_WORLD = new SimpleCommandExceptionType(() -> Lang.ERROR_MISSING_WORLD);

        /**
         * Create a new world argument.
         */
        public Argument() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }

        @Override
        @NotNull
        public World parse(@NotNull StringReader reader) throws CommandSyntaxException {
            String input = StringArgumentType.greedyString().parse(reader);
            World world = LiveMap.api().getWorldRegistry().get(input);
            if (world == null) {
                throw ERROR_INVALID_WORLD.create();
            }
            return world;
        }

        @Override
        @NotNull
        public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
            for (var entry : LiveMap.api().getWorldRegistry().entrySet()) {
                if (entry.getKey().startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(entry.getKey());
                }
                if (entry.getValue().getName().toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(entry.getValue().getName());
                }
            }
            return builder.buildFuture();
        }

        /**
         * Gets the native type that this argument uses,
         * the type that is sent to the client.
         *
         * @return native argument type
         */
        @NotNull
        public ArgumentType<String> getNativeType() {
            return StringArgumentType.greedyString();
        }
    }
}
