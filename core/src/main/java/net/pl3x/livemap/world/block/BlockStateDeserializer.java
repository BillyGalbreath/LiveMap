/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.pl3x.livemap.world.block;

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TypeDeserializer;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.pl3x.livemap.LiveMap;
import org.jetbrains.annotations.NotNull;

/**
 * Deserializer for block states.
 */
public class BlockStateDeserializer implements TypeDeserializer<BlockState> {
    /**
     * Constructs a new instance of BlockStateDeserializer.
     */
    public BlockStateDeserializer() {
        // Explicit constructor to satisfy Javadoc and linter tools
    }

    /**
     * Read blockstate from NBT.
     *
     * @param reader NBT reader
     * @return New bock state instance
     * @throws IOException if an I/O error occurs
     */
    @Override
    @NotNull
    public BlockState read(@NotNull NBTReader reader) throws IOException {
        String id = null;
        Map<String, String> properties = null;

        reader.beginCompound();

        while (reader.hasNext()) {
            switch (reader.name()) {
                case "Name" -> id = reader.nextString();
                case "Properties" -> {
                    properties = new LinkedHashMap<>();
                    reader.beginCompound();
                    while (reader.hasNext()) {
                        properties.put(reader.name(), reader.nextString());
                    }
                    reader.endCompound();
                }
                default -> reader.skip();
            }
        }

        reader.endCompound();

        if (id == null) {
            return Block.AIR.getDefaultState();
        }

        Block block = LiveMap.api().getBlockRegistry().getOrDefault(id, Block.AIR);
        return properties == null ? block.getDefaultState() : new BlockState(block, properties);
    }
}
