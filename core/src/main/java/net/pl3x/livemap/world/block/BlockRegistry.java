/*
 * This file is part of LiveMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020-2026 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pl3x.livemap.world.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.Comparator;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.ColorsConfig;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.util.PaletteRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A registry of all known blocks to be rendered.
 */
public abstract class BlockRegistry extends PaletteRegistry<Block> {
    @Override
    public void rebuild() {
        clear();

        // load blocks from cache for persistent indexes (BlockInfo)
        Path path = LiveMap.api().getTilesDir().resolve("blocks.gz");
        loadPalette(path);

        // get and sort the blocks by id
        var entries = new ObjectArrayList<>(getBlocksAndColors().object2IntEntrySet());
        entries.sort(Comparator.comparing(Object2IntMap.Entry::getKey));

        // register the blocks (that are not already registered)
        for (var entry : entries) {
            String id = entry.getKey();
            int vanilla = entry.getIntValue();

            if (!ColorsConfig.BLOCK_COLORS.containsKey(id)) {
                Logger.warn(" &7&l-&r block not in colors.yml&3:&7&o %s &r&3(&r%s&3)".formatted(id, Colors.toHex(vanilla)));
            }

            put(id, new Block(getNextIndex(id), id, vanilla));
        }

        savePalette(path);

        Logger.info(" &7&l-&r Registered &3%d&r blocks".formatted(size()));
    }

    @Override
    @NotNull
    public Block get(@NotNull Object key) {
        Block block;
        return (block = super.get(key)) == null ? Block.AIR : block;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param block Block to add to the registry.
     * @return the previous entry associated with this block's id, or
     *     {@code null} if there was no mapping for this block.
     */
    @Nullable
    public Block put(@NotNull Block block) {
        return super.put(block.getId(), block);
    }

    /**
     * Get the block ids and their vanilla map colors from the server platform.
     *
     * @return Block ids and their vanilla map colors
     */
    @NotNull
    protected abstract Object2IntOpenHashMap<String> getBlocksAndColors();
}
