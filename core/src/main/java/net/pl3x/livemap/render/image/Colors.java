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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.util.Mathf;
import org.jetbrains.annotations.NotNull;

/**
 * Color related utils.
 */
public final class Colors {
    /**
     * Colors for wheat, based on age.
     */
    public static int[] BLOCK_WHEAT_COLOR = new int[8];
    /**
     * Colors for melon/pumpkin stems, based on age.
     */
    public static int[] BLOCK_STEM_COLOR = new int[8];
    /**
     * Colors for cocoa block, based on age.
     */
    public static int[] BLOCK_COCOA_COLOR = {0x6A682E, 0x654721, 0x703715};

    /**
     * Colormap from gradient image for grass.
     */
    public static int[] COLORMAP_GRASS;
    /**
     * Colormap from gradient image for dry foliage.
     */
    public static int[] COLORMAP_DRY_FOLIAGE;
    /**
     * Colormap from gradient image for foliage.
     */
    public static int[] COLORMAP_FOLIAGE;

    static {
        for (int i = 0; i < 8; i++) {
            BLOCK_WHEAT_COLOR[i] = Colors.lerpRGB(0x007C00, 0xDCBB65, (i + 1) / 8F);
            BLOCK_STEM_COLOR[i] = rgb(i << 5, 0xFF - (i << 3), i << 2);
        }
        Path imagesDir = LiveMap.api().getWebDir().resolve("images");
        try {
            COLORMAP_GRASS = getColorsFromImage(ImageIO.read(imagesDir.resolve("grass.png").toFile()));
            COLORMAP_DRY_FOLIAGE = getColorsFromImage(ImageIO.read(imagesDir.resolve("dry_foliage.png").toFile()));
            COLORMAP_FOLIAGE = getColorsFromImage(ImageIO.read(imagesDir.resolve("foliage.png").toFile()));
        } catch (IOException e) {
            COLORMAP_GRASS = new int[0];
            COLORMAP_DRY_FOLIAGE = new int[0];
            COLORMAP_FOLIAGE = new int[0];
        }
    }

    private Colors() {
    }

    private static int[] getColorsFromImage(@NotNull BufferedImage image) {
        int[] map = new int[256 * 256];
        for (int x = 0; x < 256; ++x) {
            for (int y = 0; y < 256; ++y) {
                map[x + y * 256] = image.getRGB(x, y);
            }
        }
        return map;
    }

    /**
     * Combine channels into rgb color.
     *
     * @param red   Red channel
     * @param green Green channel
     * @param blue  Blue channel
     * @return Color
     */
    public static int rgb(int red, int green, int blue) {
        return red << 16 | green << 8 | blue;
    }

    /**
     * Combine channels into argb color.
     *
     * @param alpha Alpha channel
     * @param red   Red channel
     * @param green Green channel
     * @param blue  Blue channel
     * @return Color
     */
    public static int argb(int alpha, int red, int green, int blue) {
        return alpha(alpha, rgb(red, green, blue));
    }

    /**
     * Set alpha channel of color.
     *
     * @param alpha Alpha to set
     * @param argb  Color to set
     * @return Color with specified alpha
     */
    public static int alpha(int alpha, int argb) {
        return alpha << 24 | argb & 0xFFFFFF;
    }

    /**
     * Get color's alpha channel.
     *
     * @param argb Color
     * @return Color's alpha channel
     */
    public static int alpha(int argb) {
        return argb >> 24 & 0xFF;
    }

    /**
     * Get color's red channel.
     *
     * @param argb Color
     * @return Color's red channel
     */
    public static int red(int argb) {
        return argb >> 16 & 0xFF;
    }

    /**
     * Get color's green channel.
     *
     * @param argb Color
     * @return Color's green channel
     */
    public static int green(int argb) {
        return argb >> 8 & 0xFF;
    }

    /**
     * Get color's blue channel.
     *
     * @param argb Color
     * @return Color's blue channel
     */
    public static int blue(int argb) {
        return argb & 0xFF;
    }

    /**
     * Get the lerp using RGB color space.
     *
     * @param color0 Start color
     * @param color1 End color
     * @param delta  The delta between the start/end colors
     * @return The lerped color
     */
    public static int lerpRGB(int color0, int color1, float delta) {
        if (color0 == color1) return color0;
        if (delta >= 1F) return color1;
        if (delta <= 0F) return color0;
        return rgb(
            (int) Mathf.lerp(red(color0), red(color1), delta),
            (int) Mathf.lerp(green(color0), green(color1), delta),
            (int) Mathf.lerp(blue(color0), blue(color1), delta)
        );
    }

    /**
     * Get the lerp using ARGB color space.
     *
     * @param color0 Start color
     * @param color1 End color
     * @param delta  The delta between the start/end colors
     * @return The lerped color
     */
    public static int lerpARGB(int color0, int color1, float delta) {
        if (color0 == color1) return color0;
        if (delta >= 1F) return color1;
        if (delta <= 0F) return color0;
        return argb(
            (int) Mathf.lerp(alpha(color0), alpha(color1), delta),
            (int) Mathf.lerp(red(color0), red(color1), delta),
            (int) Mathf.lerp(green(color0), green(color1), delta),
            (int) Mathf.lerp(blue(color0), blue(color1), delta)
        );
    }

    /**
     * Get the lerp using HSB color space.
     *
     * @param color0 Start color
     * @param color1 End color
     * @param delta  The delta between the start/end colors
     * @return The lerped color
     */
    public static int lerpHSB(int color0, int color1, float delta) {
        return lerpHSB(color0, color1, delta, true);
    }

    /**
     * Get the lerp using HSB color space.
     *
     * @param color0           Start color
     * @param color1           End color
     * @param delta            The delta between the start/end colors
     * @param useShortestAngle {@code true} to lerp towards the shortest angle, otherwise {@code false}
     * @return The lerped color
     */
    public static int lerpHSB(int color0, int color1, float delta, boolean useShortestAngle) {
        float[] hsb0 = Color.RGBtoHSB(red(color0), green(color0), blue(color0), null);
        float[] hsb1 = Color.RGBtoHSB(red(color1), green(color1), blue(color1), null);
        return alpha(
            (int) Mathf.lerp(alpha(color0), alpha(color1), delta),
            Color.HSBtoRGB(
                useShortestAngle ?
                    lerpShortestAngle(hsb0[0], hsb1[0], delta) :
                    Mathf.lerp(hsb0[0], hsb1[0], delta),
                Mathf.lerp(hsb0[1], hsb1[1], delta),
                Mathf.lerp(hsb0[2], hsb1[2], delta)
            )
        );
    }

    /**
     * Get the inverse lerp using RGB color space.
     *
     * @param color0 Start color
     * @param color1 End color
     * @param delta  The delta between the start/end colors
     * @return The inverse lerped color
     */
    public static int inverseLerpRGB(int color0, int color1, float delta) {
        if (color0 == color1) return color0;
        if (delta >= 1F) return color1;
        if (delta <= 0F) return color0;
        return rgb(
            (int) Mathf.inverseLerp(red(color0), red(color1), delta),
            (int) Mathf.inverseLerp(green(color0), green(color1), delta),
            (int) Mathf.inverseLerp(blue(color0), blue(color1), delta)
        );
    }

    /**
     * Get the inverse lerp using ARGB color space.
     *
     * @param color0 Start color
     * @param color1 End color
     * @param delta  The delta between the start/end colors
     * @return The inverse lerped color
     */
    public static int inverseLerpARGB(int color0, int color1, float delta) {
        if (color0 == color1) return color0;
        if (delta >= 1F) return color1;
        if (delta <= 0F) return color0;
        return argb(
            (int) Mathf.inverseLerp(alpha(color0), alpha(color1), delta),
            (int) Mathf.inverseLerp(red(color0), red(color1), delta),
            (int) Mathf.inverseLerp(green(color0), green(color1), delta),
            (int) Mathf.inverseLerp(blue(color0), blue(color1), delta)
        );
    }

    /**
     * Get the inverse lerp using HSB color space.
     *
     * @param color0 Start color
     * @param color1 End color
     * @param delta  The delta between the start/end colors
     * @return The inverse lerped color
     */
    public static int inverseLerpHSB(int color0, int color1, float delta) {
        return inverseLerpHSB(color0, color1, delta, true);
    }

    /**
     * Get the inverse lerp using HSB color space.
     *
     * @param color0           Start color
     * @param color1           End color
     * @param delta            The delta between the start/end colors
     * @param useShortestAngle {@code true} to lerp towards the shortest angle, otherwise {@code false}
     * @return The inverse lerped color
     */
    public static int inverseLerpHSB(int color0, int color1, float delta, boolean useShortestAngle) {
        float[] hsb0 = Color.RGBtoHSB(red(color0), green(color0), blue(color0), null);
        float[] hsb1 = Color.RGBtoHSB(red(color1), green(color1), blue(color1), null);
        return alpha(
            (int) Mathf.inverseLerp(alpha(color0), alpha(color1), delta),
            Color.HSBtoRGB(
                useShortestAngle ?
                    lerpShortestAngle(hsb0[0], hsb1[0], delta) :
                    Mathf.inverseLerp(hsb0[0], hsb1[0], delta),
                Mathf.inverseLerp(hsb0[1], hsb1[1], delta),
                Mathf.inverseLerp(hsb0[2], hsb1[2], delta)
            )
        );
    }

    /**
     * Lerp towards the shortest angle.
     *
     * @param start The start value
     * @param end   The end value
     * @param delta The delta between the start/end values
     * @return Interpolated value between start and end
     */
    public static float lerpShortestAngle(float start, float end, float delta) {
        float distCW = (end >= start ? end - start : 1F - (start - end));
        float distCCW = (start >= end ? start - end : 1F - (end - start));
        float direction = (distCW <= distCCW ? distCW : -1F * distCCW);
        return (start + (direction * delta));
    }

    /**
     * Get color from hex string.
     *
     * @param hex Hex string
     * @return Color
     */
    public static int fromHex(@NotNull String hex) {
        return (int) Long.parseLong(hex.replaceAll("(?i)^0x|^#", ""), 16);
    }

    /**
     * Get hex string from color.
     *
     * @param rgb color
     * @return Hex string
     */
    @NotNull
    public static String toHex(int rgb) {
        return "#%06X".formatted(rgb & 0xFFFFFF);
    }

    /**
     * Offset color slightly to make it look "sprinkled" on the map
     *
     * @param color  Base color
     * @param amount Max offset amount
     * @return Sprinkled color
     */
    public static int sprinkle(int color, int amount) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int r = Math.clamp(red(color) + rand.nextInt(amount) - amount / 2, 0, 0xFF);
        int g = Math.clamp(green(color) + rand.nextInt(amount) - amount / 2, 0, 0xFF);
        int b = Math.clamp(blue(color) + rand.nextInt(amount) - amount / 2, 0, 0xFF);
        return argb(alpha(color), r, g, b);
    }
}
