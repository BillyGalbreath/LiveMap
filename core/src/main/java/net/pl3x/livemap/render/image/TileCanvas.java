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

package net.pl3x.livemap.render.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.render.image.io.IO;
import net.pl3x.livemap.util.FileUtil;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a tile which holds all the important data that is saved
 * to disk per region. <em>(images, heightmaps, block/biome info, etc.)</em>
 */
public class TileCanvas implements ImageInt {
    private static final Map<@NotNull Path, @NotNull ReadWriteLock> FILE_LOCKS = new ConcurrentHashMap<>();

    public static final String DIR_PATH = "%d/%s/";
    public static final String FILE_PATH = "%d_%d.%s";

    private final Region region;
    private final IO.Type io;

    private final Map<String, Image> images = new ConcurrentHashMap<>();

    private final int[] pixels = new int[512 << 9];
    private boolean dirty;

    /**
     * Constructs a new instance of Tile.
     *
     * @param region Region this tile belongs to
     */
    public TileCanvas(@NotNull Region region) {
        this.region = region;
        this.io = IO.getType(Config.WEB_TILE_FORMAT);
    }

    /**
     * Get the region for this tile.
     *
     * @return Requested region
     */
    @NotNull
    public Region getRegion() {
        return this.region;
    }

    /**
     * Get the world for this tile.
     *
     * @return Requested world
     */
    @NotNull
    public World getWorld() {
        return getRegion().getWorld();
    }

    /**
     * Get the IO mechanism that read/writes this tile from/to the disk.
     *
     * @return IO mechanism
     */
    @NotNull
    public IO.Type getIO() {
        return this.io;
    }

    /**
     * Get an image by id, or create a new one if one doesn't exist.
     *
     * @param id   The id for an image (usually it is the id of the renderer that uses it)
     * @param func The function to create a new image if one does not exist
     * @param <T>  The type of image
     * @return The image for specified id
     */
    @NotNull
    public <T extends Image> T getOrCreateImage(@NotNull String id, @NotNull ImageFunction<String, T> func) {
        return Unsafe.cast(this.images.computeIfAbsent(id, func));
    }

    @Override
    public void setPixel(int index, int value) {
        this.dirty = true;
        ImageInt.super.setPixel(index, value);
    }

    @Override
    public int[] getPixels() {
        return this.pixels;
    }

    /**
     * Save image data using in-memory consolidation.
     *
     * @param sharedCanvases Shared canvases for higher zoom levels
     */
    public void save(@NotNull Map<Path, ActiveTileCanvas> sharedCanvases) {
        if (!this.dirty) {
            return;
        }

        int zoomMax = getWorld().getConfig().ZOOM_MAX_OUT;

        for (int zoom = 0; zoom <= zoomMax; zoom++) {
            final int curZoom = zoom;

            int x = getRegion().getX() >> curZoom;
            int z = getRegion().getZ() >> curZoom;

            Path dir = getWorld().getTilesDir().resolve(DIR_PATH.formatted(curZoom, "basic")); // todo - use renderer's id
            FileUtil.createDirs(dir);
            Path file = dir.resolve(FILE_PATH.formatted(x, z, getIO().getExtension()));

            if (curZoom == 0) {
                try {
                    BufferedImage buffer = getOrCreateBuffer(file);
                    writePixels(buffer, curZoom);
                    getIO().write(file, buffer);
                } catch (Throwable t) {
                    Logger.error("Failed writing base tile: " + file, t);
                }
                continue;
            }

            // for higher zoom levels (1, 2, 3), consolidate modifications in memory via CHM
            // computeIfAbsent is atomic, ensuring all threads bind to the exact same shared image canvas instance
            ActiveTileCanvas canvas = sharedCanvases.computeIfAbsent(file,
                _ -> new ActiveTileCanvas(this, curZoom)
            );

            // Synchronize on the canvas to safely draw pixel matrices from multiple threads
            synchronized (canvas) {
                writePixels(canvas.getImageBuffer(), zoom);
            }

            // record this thread's contribution. the very last thread to finish writing
            // its quadrant triggers true, removes the entry from the map, and saves it to the disk.
            if (canvas.recordContribution()) {
                sharedCanvases.remove(file); // purge from memory to prevent leaks
                try {
                    getIO().write(file, canvas.getImageBuffer());
                } catch (Throwable t) {
                    Logger.error("Failed flushing consolidated tile to disk: " + file, t);
                }
            }
        }

        this.dirty = false;
    }

    @NotNull
    private BufferedImage getOrCreateBuffer(@NotNull Path path) throws IOException {
        BufferedImage buffer = null;

        // try to read existing image
        if (Files.exists(path) && Files.size(path) > 0) {
            buffer = getIO().read(path);
        }

        // if not, create a new image
        if (buffer == null) {
            buffer = getIO().createBuffer();
        }

        return buffer;
    }

    private void writePixels(@NotNull BufferedImage buffer, int zoom) {
        // how many pixels to increment in each direction
        int step = 1 << zoom;

        // number of colors to blend per step
        int count = 1 << (zoom << 1);

        // calculate where in the buffer do we start writing pixels.
        // zoom level increments the number of regions in a single tile,
        // so we want to ensure we are only writing in this tile region's
        // section of the buffer
        int baseX = (getRegion().getX() * (TileCanvas.SIZE >> zoom)) & TileCanvas.MASK;
        int baseZ = (getRegion().getZ() * (TileCanvas.SIZE >> zoom)) & TileCanvas.MASK;

        // walk the pixels
        for (int x = 0; x < TileCanvas.SIZE; x += step) {
            for (int z = 0; z < TileCanvas.SIZE; z += step) {
                int argb;

                if (zoom == 0) {
                    // current pixel as-is on base zoom level
                    argb = getPixel(x, z);
                } else {
                    // downsample merge pixels if we are at higher zoom than base
                    argb = downSample(x, z, step, count);
                }

                // write pixel to buffer for region at specified zoom
                buffer.setRGB(
                    baseX + (x >> zoom),
                    baseZ + (z >> zoom),
                    getIO().color(argb) // ensure we use a color format the image buffer supports
                );
            }
        }
    }

    private int downSample(int x, int z, int step, int count) {
        int rgb, a = 0, r = 0, g = 0, b = 0;
        for (int i = 0; i < step; i++) {
            for (int j = 0; j < step; j++) {
                rgb = getPixel(x + i, z + j);
                a += (rgb >>> 24);
                r += (rgb >> 16 & 0xFF);
                g += (rgb >> 8 & 0xFF);
                b += (rgb & 0xFF);
            }
        }
        return ((a / count) << 24)
            | ((r / count) << 16)
            | ((g / count) << 8)
            | (b / count);
    }
}
