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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongLists;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class to handle file operations.
 */
public final class FileUtil {
    public static final PathMatcher JSON_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**/*.json");
    public static final PathMatcher MCA_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**/r.*.*.mca");

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
     * Extract file from LiveMap jar to disk.
     *
     * @param filename File to extract (in jar path)
     * @param outDir   Directory of destination (on disk path)
     * @param replace  True to relace existing file on disk
     */
    public static void extractFile(@NotNull String filename, @NotNull Path outDir, boolean replace) {
        try (InputStream in = Config.class.getResourceAsStream("/%s".formatted(filename))) {
            if (in == null) {
                throw new RuntimeException("Could not read file from jar! (" + filename + ")");
            }
            Path path = outDir.resolve(filename);
            if (!Files.exists(path) || replace) {
                Files.createDirectories(path.getParent());
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Create directory, including any parent directories needed.
     *
     * @param dirPath Directory path
     */
    public static void createDirs(@NotNull Path dirPath) {
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Create a path to a temporary file.
     *
     * <p>File starts with {@code .} and ends with {@code .tmp} (i.e., {@code .0_0.png.tmp})
     *
     * @param filename Name of file
     * @return Temporary file path
     */
    @NotNull
    public static Path tmp(@NotNull Path filename) {
        return filename.resolveSibling("." + filename.getFileName().toString() + ".tmp");
    }

    /**
     * Atomically move a file.
     *
     * @param source Source file to move from
     * @param target Target file to move to
     * @throws IOException if an I/O error occurs
     */
    public static void atomicMove(@NotNull Path source, @NotNull Path target) throws IOException {
        try {
            atomicMove(source, target, 0);
        } catch (AccessDeniedException | NoSuchFileException ignore) {
        }
    }

    private static void atomicMove(@NotNull Path source, @NotNull Path target, int attempt) throws IOException {
        try {
            com.google.common.io.Files.move(source.toFile(), target.toFile());
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (AccessDeniedException e) {
            if (attempt < 5) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignore) {
                }
                atomicMove(source, target, ++attempt);
            } else if (source.getFileName().toString().endsWith(".tmp")) {
                try {
                    Files.delete(source);
                } catch (Throwable ignore) {
                }
            }
        }
    }

    /**
     * Get region file paths for specified world.
     *
     * @param world World to check
     * @return Collection of file paths
     */
    @NotNull
    public static Collection<Path> getRegionPaths(@NotNull World world) {
        if (!Files.exists(world.getRegionsDir())) {
            return Collections.emptySet();
        }
        try (Stream<Path> stream = Files.list(world.getRegionsDir())) {
            return stream.filter(MCA_MATCHER::matches).toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to list region files in directory '" + world.getRegionsDir().toAbsolutePath() + "'", e);
        }
    }

    /**
     * Convert a collection of region file paths to a collection of longs.
     *
     * @param paths Paths to convert
     * @return Collection of longs
     */
    @NotNull
    public static LongCollection regionPathsToLongs(@Nullable Collection<Path> paths) {
        if (paths == null || paths.isEmpty()) {
            return LongLists.emptyList();
        }
        LongCollection regions = new LongArrayList();
        for (Path file : paths) {
            if (file.toFile().length() <= 0) {
                Logger.debug("Skipping zero length region file: " + file.getFileName());
                continue;
            }
            try {
                String[] split = file.getFileName().toString().split("\\.");
                int rX = Integer.parseInt(split[1]);
                int rZ = Integer.parseInt(split[2]);
                regions.add(Region.pack(rX, rZ));
            } catch (NumberFormatException ignore) {
            }
        }
        return regions;
    }
}
