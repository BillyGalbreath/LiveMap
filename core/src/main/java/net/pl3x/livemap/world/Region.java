package net.pl3x.livemap.world;

import java.nio.file.Path;
import java.util.Objects;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.world.chunk.Chunk;
import net.pl3x.livemap.world.chunk.EmptyChunk;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents a region in a world.
 */
public class Region extends Point {
    /**
     * Packs a region's coordinates.
     *
     * @param regionX X region coordinate
     * @param regionZ Z region coordinate
     * @return Region's packed coordinates
     */
    public static long pack(int regionX, int regionZ) {
        // 64 bits; upper 32 are z; lower 32 are x
        return ((long) regionX << 32) | (regionZ & 0xFFFFFFFFL);
    }

    /**
     * Returns the X value from packed region coordinates.
     *
     * @param packed Packed region coordinates
     * @return Unpacked X coordinate
     */
    public static int unpackX(long packed) {
        // lower 32 bits are x
        return (int) packed;
    }

    /**
     * Returns the Z value from packed region coordinates.
     *
     * @param packed Packed region coordinates
     * @return Unpacked Z coordinate
     */
    public static int unpackZ(long packed) {
        // upper 32 bits are z
        return (int) (packed >>> 32);
    }

    private final World world;
    private final Path file;

    private final Chunk[] chunks = new Chunk[1024];

    private final int hash;

    /**
     * Constructs a new instance of Region.
     *
     * @param world  Owning world
     * @param packed Packed region coordinates
     */
    public Region(@NotNull World world, long packed) {
        this(world, unpackX(packed), unpackZ(packed));
    }

    /**
     * Constructs a new instance of Region.
     *
     * @param world   Owning world
     * @param regionX X coordinate
     * @param regionZ Z coordinate
     */
    public Region(@NotNull World world, int regionX, int regionZ) {
        super(regionX, regionZ);
        this.world = world;
        this.file = this.world.getRegionsDir().resolve("r.%d.%d.mca".formatted(regionX, regionZ));

        this.hash = Objects.hash(world, regionX, regionZ);
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

    /**
     * Get chunk at specified chunk coordinates.
     * <p>
     * If no chunk exists there, an EmptyChunk will be returned.
     *
     * @param chunkX X chunk coordinate
     * @param chunkZ Z chunk Coordinate
     * @return Requested chunk
     */
    @NotNull
    public Chunk getChunk(int chunkX, int chunkZ) {
        return new EmptyChunk(this);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Region other = (Region) o;
        return getWorld().equals(other.getWorld())
            && getX() == other.getX()
            && getZ() == other.getZ();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    @NotNull
    public String toString() {
        return "Region["
            + "world=" + getWorld()
            + ",x=" + getX()
            + ",z=" + getZ()
            + "]";
    }
}
