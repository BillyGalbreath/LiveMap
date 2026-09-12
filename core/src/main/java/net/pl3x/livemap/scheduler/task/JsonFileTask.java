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

package net.pl3x.livemap.scheduler.task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.scheduler.Task;
import net.pl3x.livemap.thread.WorkerThreadFactory;
import net.pl3x.livemap.util.FileUtil;
import net.pl3x.livemap.util.Unsafe;
import org.jetbrains.annotations.NotNull;

/**
 * Task for writing JSON data to disk on a schedule.
 */
public abstract class JsonFileTask extends Task {
    protected static final int THIRTY_SECONDS_IN_TICKS = 30 * 20;

    protected static final ExecutorService EXECUTOR = WorkerThreadFactory.createExecutor("Json-Writer");

    protected static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .setStrictness(Strictness.LENIENT)
        .create();

    protected final Path path;
    private int cachedHash;

    private CompletableFuture<Void> future;
    private boolean running;

    /**
     * Constructs a new instance of JsonFileTask.
     *
     * @param path Path to JSON file to write
     */
    public JsonFileTask(@NotNull Path path) {
        super(/*THIRTY_SECONDS_IN_TICKS*/ 100, true); // todo
        this.path = path;

        // load existing json to get its hash
        this.cachedHash = FileUtil.readString(this.path).hashCode();
    }

    @Override
    public void cancel() {
        super.cancel();
        if (this.future != null) {
            this.future.cancel(true);
        }
    }

    @Override
    public void run() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.future = CompletableFuture.runAsync(() -> {
                try {
                    writeJson(createJson());
                } catch (Throwable t) {
                    Logger.error("Failed to write %s".formatted(this.path), t);
                }
            }, EXECUTOR)
            .whenComplete((_, _) -> {
                //
                this.running = false;
            });
    }

    /**
     * Create JSON data.
     *
     * @return String representation of JSON data
     */
    @NotNull
    protected abstract String createJson();

    /**
     * Write JSON data to disk.
     *
     * @param json The JSON data to write
     */
    protected void writeJson(@NotNull String json) {
        int hash = json.hashCode();
        if (hash == this.cachedHash) {
            Logger.debug("File %s has not changed, skipping write to disk".formatted(this.path));
            return;
        }

        Logger.debug("Writing %s to disk".formatted(this.path));
        FileUtil.writeString(json, this.path);
        this.cachedHash = hash;
    }

    /**
     * Create a new LinkedHashMap from the specified mappings (similar to {@link Map#of()}).
     *
     * @param pairs The key value pairs
     * @param <K>   Type of keys
     * @param <V>   Type of values
     * @return LinkedHashMap of key value pairs
     */
    @NotNull
    protected <K, V> Map<K, V> mapOf(@NotNull Object... pairs) {
        if ((pairs.length & 1) != 0) {
            throw new InternalError("length is odd");
        }
        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(Unsafe.cast(pairs[i]), Unsafe.cast(pairs[i + 1]));
        }
        return map;
    }
}
