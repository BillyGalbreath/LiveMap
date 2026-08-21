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
                    continue;
                }
                if (entry.isDirectory()) {
                    if (exists) {
                        continue;
                    }
                    try {
                        Files.createDirectories(file);
                    } catch (IOException ignore) {
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
                } catch (IOException e) {
                    Logger.error("Failed to extract file (" + name + ") from jar!", e);
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to extract " + sourceDir + " directory from jar", e);
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
            Logger.error("Error reading file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
