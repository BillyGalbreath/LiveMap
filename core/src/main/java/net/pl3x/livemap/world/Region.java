package net.pl3x.livemap.world;

import java.nio.file.Path;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a region in a world.
 */
public class Region extends Coordinates {
    /**
     * Mask for 32-bit integers.
     */
    public static final long INT_MASK = 0xFFFFFFFFL;

    private final World world;
    private final Path file;

    private final Chunk[] chunks = new Chunk[1024];

    /**
     * Packs a region's coordinates.
     *
     * @param x X region coordinate
     * @param z Z region coordinate
     * @return Region's packed coordinates
     */
    public static long pack(int x, int z) {
        return (long) x & INT_MASK | ((long) z & INT_MASK) << 32;
    }

    /**
     * Returns the X value from packed region coordinates.
     *
     * @param packed Packed region coordinates
     * @return Unpacked X coordinate
     */
    public static int unpackX(long packed) {
        return (int) (packed & INT_MASK);
    }

    /**
     * Returns the Z value from packed region coordinates.
     *
     * @param packed Packed region coordinates
     * @return Unpacked Z coordinate
     */
    public static int unpackZ(long packed) {
        return (int) (packed >>> 32 & INT_MASK);
    }

    /**
     * Constructs a new instance of Region.
     *
     * @param world  Owning world
     * @param packed Packed coordinates
     */
    public Region(@NotNull World world, long packed) {
        this(world, unpackX(packed), unpackZ(packed));
    }

    /**
     * Constructs a new instance of Region.
     *
     * @param world Owning world
     * @param x     X coordinate
     * @param z     Z coordinate
     */
    public Region(@NotNull World world, int x, int z) {
        super(x, z);
        this.world = world;
        this.file = this.world.getRegionsDir().resolve("r.%d.%d.mca".formatted(x, z));
    }

    /**
     * Get the world this region belongs to.
     *
     * @return Owning world
     */
    @NotNull
    public World getWorld() {
        return this.world;
    }

    /**
     * Get the region's MCA file path.
     *
     * @return Region's MCA file path
     */
    @NotNull
    public Path getFile() {
        return this.file;
    }
}
