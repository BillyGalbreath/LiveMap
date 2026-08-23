package net.pl3x.livemap.world.block;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a state of a block.
 */
public class BlockState {
    private static byte parseProperty(@NotNull String property) {
        try {
            return Integer.valueOf(property).byteValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private final Block block;
    private final byte age;
    private final byte moisture;
    private final byte power;

    /**
     * Constructs a new instance of BlockState with no properties.
     *
     * @param block Block represented by this state
     */
    public BlockState(@NotNull Block block) {
        this.block = block;
        this.age = this.moisture = this.power = -1;
    }

    /**
     * Constructs a new instance of BlockState with specified properties.
     *
     * @param block      Block represented by this state
     * @param properties Properties for this state
     */
    public BlockState(@NotNull Block block, @NotNull Map<String, String> properties) {
        this.block = block;
        this.age = parseProperty(properties.get("age"));
        this.moisture = parseProperty(properties.get("moisture"));
        this.power = parseProperty(properties.get("power"));
    }

    /**
     * Gets the block represented by this state.
     *
     * @return the block represented by this state
     */
    @NotNull
    public Block getBlock() {
        return this.block;
    }

    /**
     * Get state's age.
     *
     * @return Age, or -1 if n/a
     */
    public byte getAge() {
        return this.age;
    }

    /**
     * Get state's moisture level.
     *
     * @return Moisture level, or -1 if n/a
     */
    public byte getMoisture() {
        return this.moisture;
    }

    /**
     * Get state's power output.
     *
     * @return Power output, or -1 if n/a
     */
    public byte getPower() {
        return this.power;
    }
}
