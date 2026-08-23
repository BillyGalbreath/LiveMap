package net.pl3x.livemap.world.chunk;

import net.pl3x.livemap.world.Region;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.block.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an empty or (non-existent) chunk.
 */
public class EmptyChunk extends Chunk {
    /**
     * Constructs a new instance of EmptyChunk.
     *
     * @param region Region chunk belongs to
     */
    public EmptyChunk(@NotNull Region region) {
        super(region, new NBT());
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public long getInhabitedTime() {
        return 0;
    }

    @Override
    public int getHeight(int blockX, int blockZ) {
        return 0;
    }

    @Override
    @NotNull
    public BlockState getBlockState(int blockX, int blockY, int blockZ) {
        return Block.AIR.getDefaultState();
    }

    @Override
    @NotNull
    public Biome getBiome(int blockX, int blockY, int blockZ) {
        return Biome.DEFAULT;
    }

    @Override
    public int getLight(int blockX, int blockY, int blockZ) {
        return 0;
    }

    @Override
    @NotNull
    public String toString() {
        return "EmptyChunk["
            + "world=" + getWorld()
            + ",xPos=" + getX()
            + ",yPos=" + getY()
            + ",zPos=" + getZ()
            + "]";
    }
}
