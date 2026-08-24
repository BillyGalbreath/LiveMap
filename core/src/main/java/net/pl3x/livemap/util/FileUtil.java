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

package net.pl3x.livemap.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to handle file operations.
 */
public final class FileUtil {
    private static Path jarPath;

    private FileUtil() {
    }

    /**
     * Get the path of the LiveMap jar file.
     *
     * @return Path to jar file
     */
    @NotNull
    public static Path getJarPath() {
        if (jarPath == null) {
            try {
                jarPath = Path.of(LiveMap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return jarPath;
    }

    /**
     * Extract directory from LiveMap jar to disk.
     *
     * @param sourceDir Directory of source (in jar path)
     * @param outDir    Directory of destination (on disk path)
     * @param replace   {@code true} to replace any files or directories
     */
    public static void extractDir(@NotNull String sourceDir, @NotNull Path outDir, boolean replace) {
        try (JarFile jarFile = new JarFile(getJarPath().toFile())) {
            Logger.debug("Extracting &3%s&r directory from jar...".formatted(sourceDir));
            String path = sourceDir.substring(1);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(path)) {
                    continue;
                }
                Path file = outDir.resolve(name.substring(path.length()));
                boolean exists = Files.exists(file);
                if (!replace && exists) {
                    Logger.debug("  &eexists&r   %s".formatted(name));
                    continue;
                }
                if (entry.isDirectory()) {
                    if (exists) {
                        Logger.debug("  &eexists&r   %s".formatted(name));
                        continue;
                    }
                    try {
                        Files.createDirectories(file);
                        Logger.debug("  &acreating&r %s".formatted(name));
                    } catch (IOException e) {
                        Logger.debug("  &c&lfailed&r   %s".formatted(name));
                        Logger.error("Failed to create directory &3(&e%s&3)".formatted(name), e);
                    }
                    continue;
                }
                try (
                    InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(file.toFile()))
                ) {
                    byte[] buffer = new byte[4096];
                    int readCount;
                    while ((readCount = in.read(buffer)) > 0) {
                        out.write(buffer, 0, readCount);
                    }
                    out.flush();
                    Logger.debug("  &awriting&r  %s".formatted(name));
                } catch (IOException e) {
                    Logger.debug("  &c&lfailed&r   %s".formatted(name));
                    Logger.error("Failed to extract file &3(&e%s&3)&r from jar!".formatted(name), e);
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to extract &o%s&r directory from jar!".formatted(sourceDir), e);
        }
    }

    /**
     * Read a string from a file.
     *
     * @param path Path to file
     * @return String read from file
     */
    @NotNull
    public static String readString(@NotNull Path path) {
        try {
            return Files.exists(path) ? Files.readString(path) : "";
        } catch (IOException e) {
            Logger.error("Error reading file&3:&r %s".formatted(e.getMessage()), e);
            throw new RuntimeException(e);
        }
    }
}
