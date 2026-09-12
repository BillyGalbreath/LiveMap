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
import java.awt.image.DataBufferInt;
import java.nio.file.Path;
import java.util.Map;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.render.heightmap.Heightmap;
import net.pl3x.livemap.render.image.io.IO;
import net.pl3x.livemap.render.renderer.Renderer;
import net.pl3x.livemap.util.FileUtil;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a tile which holds all the important data that is saved
 * to disk per region. <em>(images, heightmaps, block/biome info, etc.)</em>
 */
public class TileCanvas {
    public static final String DIR_PATH = "%d/%s/";
    public static final String FILE_PATH = "%d_%d.%s";

    private static final ThreadLocal<BufferedImage> THREAD_LOCAL_BASE_BUFFER = new ThreadLocal<>();

    private final Region region;
    private final Renderer renderer;
    private final IO.Type io;

    private final Heightmap heightmap;

    private final int[] pixels = new int[512 << 9];
    protected boolean dirty;

    /**
     * Constructs a new instance of TileCanvas.
     *
     * @param region   Region this tile belongs to
     * @param renderer The renderer drawing on this tile
     */
    public TileCanvas(@NotNull Region region, @NotNull Renderer renderer) {
        this(region, renderer, IO.getType(Config.WEB_TILE_FORMAT));
    }

    /**
     * Constructs a new instance of TileCanvas.
     *
     * @param region   Region this tile belongs to
     * @param renderer The renderer drawing on this tile
     * @param io       The IO type for reading/writing images
     */
    public TileCanvas(@NotNull Region region, @NotNull Renderer renderer, @NotNull IO.Type io) {
        this.region = region;
        this.renderer = renderer;
        this.io = io;

        this.heightmap = renderer.getHeightmapType().create();
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
     * Get the region for this tile.
     *
     * @return Requested region
     */
    @NotNull
    public Region getRegion() {
        return this.region;
    }

    /**
     * Get the renderer that's drawing on this tile.
     *
     * @return The renderer
     */
    @NotNull
    public Renderer getRenderer() {
        return this.renderer;
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
     * Get the heightmap.
     *
     * @return The heightmap
     */
    @NotNull
    public Heightmap getHeightmap() {
        return this.heightmap;
    }

    /**
     * Get value at specified pixel.
     *
     * @param index Pixel index
     * @return Requested value
     */
    public int getPixel(int index) {
        return this.pixels[index];
    }

    /**
     * Set pixel to specified value.
     *
     * @param x     X pixel
     * @param z     Z pixel
     * @param value Value to set
     */
    public void setPixel(int x, int z, int value) {
        setPixel(((z & 511) << 9) | (x & 511), value);
    }

    /**
     * Set pixel to specified value.
     *
     * @param index Pixel index
     * @param value Value to set
     */
    public void setPixel(int index, int value) {
        this.pixels[index] = value;
        this.dirty = true;
    }

    /**
     * Save image data using in-memory consolidation.
     *
     * @param zoomedCanvases Zoomed canvases for higher zoom levels
     */
    public void save(@NotNull Map<Path, ZoomedCanvas> zoomedCanvases) {
        if (!this.dirty) {
            return;
        }

        int zoomMax = getWorld().getConfig().ZOOM_MAX_OUT;

        for (int zoom = 0; zoom <= zoomMax; zoom++) {
            final int curZoom = zoom;

            int x = getRegion().getX() >> curZoom;
            int z = getRegion().getZ() >> curZoom;

            Path dir = getWorld().getTilesDir().resolve(DIR_PATH.formatted(curZoom, getRenderer().getId()));
            FileUtil.createDirs(dir);
            Path file = dir.resolve(FILE_PATH.formatted(x, z, getIO().getExtension()));

            if (curZoom == 0) {
                try {
                    BufferedImage buffer = getBaseBuffer();
                    writePixels(buffer, curZoom);
                    getIO().write(file, buffer);
                } catch (Throwable t) {
                    Logger.error("Failed writing base tile: " + file, t);
                }
                continue;
            }

            // for higher zoom levels (1, 2, 3), consolidate modifications in memory via CHM
            // computeIfAbsent is atomic, ensuring all threads bind to the exact same shared image canvas instance
            ZoomedCanvas zoomedCanvas = zoomedCanvases.computeIfAbsent(file, _ -> createZoomedCanvas(curZoom));

            // synchronize on the canvas to safely draw pixel matrices from multiple threads
            synchronized (zoomedCanvas) {
                writePixels(zoomedCanvas.getImageBuffer(), zoom);

                // record this thread's contribution. the very last thread to finish writing
                // its quadrant triggers true, removes the entry from the map, and saves it to the disk.
                if (zoomedCanvas.recordContribution()) {
                    zoomedCanvases.remove(file); // purge from memory to prevent leaks
                    try {
                        getIO().write(file, zoomedCanvas.getImageBuffer());
                    } catch (Throwable t) {
                        Logger.error("Failed flushing consolidated tile to disk: " + file, t);
                    }
                }
            }
        }

        this.dirty = false;
    }

    /**
     * Get the BufferedImage for the zoom 0 base image.
     *
     * @return Base BufferedImage
     */
    @NotNull
    protected BufferedImage getBaseBuffer() {
        BufferedImage buffer = THREAD_LOCAL_BASE_BUFFER.get();
        if (buffer == null || buffer.getType() != getIO().colorType()) {
            buffer = getIO().createBuffer();
            THREAD_LOCAL_BASE_BUFFER.set(buffer);
        }
        return buffer;
    }

    /**
     * Create a new ZoomedCanvas for higher than 0 zoom levels.
     *
     * @param zoom Zoom level of canvas
     * @return A new ZoomedCanvas
     */
    @NotNull
    protected ZoomedCanvas createZoomedCanvas(int zoom) {
        return new ZoomedCanvas(getIO().createBuffer(), zoom);
    }

    /**
     * Write the tile's stored pixels to the BufferedImage at specified zoom level.
     *
     * @param buffer BufferedImage to write to
     * @param zoom   Zoom level
     */
    protected void writePixels(@NotNull BufferedImage buffer, int zoom) {
        int[] bufferPixels = Unsafe.<DataBufferInt>cast(buffer.getRaster().getDataBuffer()).getData();

        if (zoom == 0) {
            System.arraycopy(this.pixels, 0, bufferPixels, 0, this.pixels.length);
            return;
        }

        // how many pixels to increment in each direction
        int step = 1 << zoom;

        // calculate where in the buffer do we start writing pixels.
        // zoom level increments the number of regions in a single tile,
        // so we want to ensure we are only writing in this tile region's
        // section of the buffer
        int baseX = (getRegion().getX() * (512 >> zoom)) & 511;
        int baseZ = (getRegion().getZ() * (512 >> zoom)) & 511;

        // walk the pixels
        for (int z = 0; z < 512; z += step) {
            int targetZ = baseZ + (z >> zoom);
            int targetRowOffset = targetZ * 512;
            for (int x = 0; x < 512; x += step) {
                int targetX = baseX + (x >> zoom);
                int argb = downSample(x, z, step, zoom);
                bufferPixels[targetRowOffset + targetX] = getIO().color(argb);
            }
        }
    }

    private int downSample(int x, int z, int step, int zoom) {
        int rgb, a = 0, r = 0, g = 0, b = 0;
        for (int j = 0; j < step; j++) {
            int rowOffset = (z + j) << 9;
            for (int i = 0; i < step; i++) {
                rgb = this.pixels[rowOffset + (x + i)];
                a += (rgb >>> 24);
                r += (rgb >> 16 & 0xFF);
                g += (rgb >> 8 & 0xFF);
                b += (rgb & 0xFF);
            }
        }
        int shift = zoom << 1;
        return ((a >> shift) << 24)
            | ((r >> shift) << 16)
            | ((g >> shift) << 8)
            | (b >> shift);
    }
}
