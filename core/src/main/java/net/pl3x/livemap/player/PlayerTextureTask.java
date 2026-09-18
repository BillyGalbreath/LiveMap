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

package net.pl3x.livemap.player;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.imageio.ImageIO;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.command.Player;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.scheduler.Task;
import net.pl3x.livemap.thread.WorkerThreadFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Task that fetches a player's texture from their game profile and saves it to the web directory.
 *
 * <p>This allows us to support offline-mode servers by using the skins directly from the profile that may
 * be set by other mods/plugins like SkinsRestorer, rather than a 3rd party web services like Crafatar.
 */
public class PlayerTextureTask extends Task {
    private static final ExecutorService EXECUTOR = WorkerThreadFactory.createExecutor("Skin-Fetcher");

    private static final double DEG_30_IN_RAD = Math.toRadians(30); // 0.523599
    private static final double COS_30_IN_DEG = Math.cos(DEG_30_IN_RAD); // 0.86602540378
    private static final double TAN_30_IN_DEG = Math.tan(DEG_30_IN_RAD); // 0.577375

    private static final Path SKINS_2D_DIR = LiveMap.api().getWebDir().resolve("images/skins/2D");
    private static final Path SKINS_3D_DIR = LiveMap.api().getWebDir().resolve("images/skins/3D");
    private static final URL DEFAULT_STEVE_SKIN;

    static {
        try {
            Files.createDirectories(SKINS_2D_DIR);
            Files.createDirectories(SKINS_3D_DIR);
            DEFAULT_STEVE_SKIN = LiveMap.api().getWebDir().resolve("images/skins/steve.png").toUri().toURL();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Player player;
    private final URL url;

    /**
     * Constructs a new instance of PlayerTexture.
     *
     * @param player Owning player
     */
    public PlayerTextureTask(@NotNull Player player) {
        super(0, false);
        this.player = player;
        URL url = player.getSkin();
        if (url == null) {
            url = DEFAULT_STEVE_SKIN;
        }
        this.url = url;
    }

    @Override
    public void run() {
        if (this.url != null) {
            CompletableFuture.runAsync(this::runAsync, EXECUTOR);
        }
    }

    private void runAsync() {
        try {
            BufferedImage fullBody = ImageIO.read(this.url);

            BufferedImage head2D = get2DHead(fullBody);
            ImageIO.write(head2D, "png", SKINS_2D_DIR.resolve(this.player.getUUID() + ".png").toFile());

            BufferedImage head3D;
            try {
                head3D = get3DHead(fullBody);
            } catch (NoClassDefFoundError e) {
                Logger.warn("Could not generate 3D head for %s. Falling back to 2D head.".formatted(this.player.getName()), e);
                // happens in headless environments (missing AWT's GraphicsEnvironment)
                // just draw a 2d head and put it in the 3d directory for now
                head3D = head2D;
            }
            ImageIO.write(head3D, "png", SKINS_3D_DIR.resolve(this.player.getUUID() + ".png").toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static BufferedImage get2DHead(@NotNull BufferedImage source) {
        // scale it up to 32
        return scale(getPart(source, 8, 8), 4, 4);
    }

    @NotNull
    private static BufferedImage get3DHead(@NotNull BufferedImage source) {
        // get parts
        BufferedImage front = getPart(source, 8, 8);
        BufferedImage left = getPart(source, 16, 8);
        BufferedImage top = getPart(source, 8, 0);

        // scale parts up to 512
        front = scale(front, 64, 64 * COS_30_IN_DEG);
        left = scale(left, 64, 64 * COS_30_IN_DEG);
        top = scale(top, 64, 64 * COS_30_IN_DEG);

        // shear parts
        front = shear(front);
        left = flip(shear(flip(left)));
        top = flip(shear(flip(top)));

        // rotate parts
        front = rotate(front, DEG_30_IN_RAD);
        left = rotate(left, -DEG_30_IN_RAD);
        top = rotate(top, DEG_30_IN_RAD);

        // combine parts (884x765)
        BufferedImage result = new BufferedImage(1024, 1024, front.getType());
        Graphics2D g2d = result.createGraphics();
        g2d.translate(-151, 257);
        g2d.drawImage(front, 0, 0, null);
        g2d.translate(442, 0);
        g2d.drawImage(left, 0, 0, null);
        g2d.translate(-222, -382);
        g2d.drawImage(top, 0, 0, null);
        g2d.dispose();

        // scale result down to 128
        result = scale(result, 0.125D, 0.125D);

        return result;
    }

    @NotNull
    private static BufferedImage getPart(@NotNull BufferedImage source, int x, int y) {
        BufferedImage head = source.getSubimage(x, y, 8, 8);
        BufferedImage helm = source.getSubimage(x + 32, y, 8, 8);
        BufferedImage result = new BufferedImage(8, 8, source.getType());
        for (int x1 = 0; x1 < 8; x1++) {
            for (int z1 = 0; z1 < 8; z1++) {
                int argb = Colors.blend(
                    helm.getRGB(x1, z1),
                    head.getRGB(x1, z1)
                );
                result.setRGB(x1, z1, argb);
            }
        }
        return result;
    }

    @NotNull
    private static BufferedImage flip(@NotNull BufferedImage src) {
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(-1, 1));
        at.concatenate(AffineTransform.getTranslateInstance(-src.getWidth(), 0));
        return transform(src, at);
    }

    @NotNull
    private static BufferedImage rotate(@NotNull BufferedImage src, double angle) {
        int w = src.getWidth();
        int h = src.getHeight();
        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));
        int newWidth = (int) (w * cos + h * sin);
        int newHeight = (int) (h * cos + w * sin);

        BufferedImage dest = new BufferedImage(newWidth, newHeight, src.getType());

        Graphics2D g2d = dest.createGraphics();
        g2d.translate((newWidth - w) / 2D, (newHeight - h) / 2D);
        g2d.rotate(angle, w / 2D, h / 2D);
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();

        return dest;
    }

    @NotNull
    private static BufferedImage scale(@NotNull BufferedImage src, double scaleX, double scaleY) {
        return transform(src, AffineTransform.getScaleInstance(scaleX, scaleY));
    }

    @NotNull
    private static BufferedImage shear(@NotNull BufferedImage src) {
        return transform(src, AffineTransform.getShearInstance(TAN_30_IN_DEG, 0));
    }

    @NotNull
    private static BufferedImage transform(@NotNull BufferedImage src, @NotNull AffineTransform at) {
        return new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(src, null);
    }
}
