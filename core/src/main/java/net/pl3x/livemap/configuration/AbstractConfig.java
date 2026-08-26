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

package net.pl3x.livemap.configuration;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.pl3x.livemap.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

/**
 * Represents a base YAML configuration.
 */
public abstract class AbstractConfig {
    private final Path path;
    private YamlFile yaml;

    /**
     * Constructs a new instance of AbstractConfig.
     *
     * @param path Path to config file
     */
    protected AbstractConfig(@NotNull Path path) {
        this.path = path;
    }

    /**
     * Gets the direct YAML configuration for this config.
     *
     * @return YAML configuration
     */
    @NotNull
    public YamlFile getConfig() {
        return Objects.requireNonNullElseGet(this.yaml,
            () -> this.yaml = new YamlFile(this.path.toFile()));
    }

    /**
     * Reloads configuration from YAML file.
     */
    protected void reload0() {
        // read yaml from file
        try {
            getConfig().createOrLoadWithComments();
        } catch (InvalidConfigurationException e) {
            Logger.error("Could not load &3%s&r, please correct your syntax errors".formatted(this.path.getFileName()), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // populate keyed fields from yaml data
        Arrays.stream(getClass().getDeclaredFields()).forEach(field -> {
            Key key = field.getDeclaredAnnotation(Key.class);
            if (key == null) {
                return;
            }
            try {
                Object value = getAndSetDefault(key.value(), field.get(this));
                field.set(this, value instanceof String str ? str.translateEscapes() : value);
                Comment comment = field.getDeclaredAnnotation(Comment.class);
                if (comment != null) {
                    setComment(key.value(), comment.value());
                }
            } catch (Throwable e) {
                Logger.warn("Failed to load &3%s&r from &3%s&r".formatted(key.value(), this.path.getFileName().toString()), e);
            }
        });
    }

    /**
     * Saves the config in memory to disk.
     */
    protected void save() {
        try {
            getConfig().save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the requested Object by path.
     * <p>
     * If the Object does not exist, this will set and return the default value.
     *
     * @param path Path of the Object to get
     * @param def  The default value to return if the path is not found
     * @return Requested Object
     */
    @Nullable
    protected Object getAndSetDefault(@NotNull String path, @Nullable Object def) {
        if (getConfig().get(path) == null) {
            set(path, def);
        }
        return get(path, def);
    }

    /**
     * Sets the specified path to the given value.
     * <p>
     * If value is null, the entry will be removed. Any existing entry will be
     * replaced, regardless of what the new value is.
     *
     * @param path  Path of the object to set
     * @param value value to set the path to
     */
    protected void set(@NotNull String path, @Nullable Object value) {
        getConfig().set(path, value);
    }

    /**
     * Gets the requested Object by path.
     * <p>
     * If the Object does not exist but a default value has been specified, this will return the default value. If the Object does not exist and no default value was specified, this will return null.
     *
     * @param path Path of the Object to get
     * @param def  The default value to return if the path is not found
     * @return Requested Object
     */
    @Nullable
    protected Object get(@NotNull String path, @Nullable Object def) {
        Object val = get(path);
        return val == null ? def : val;
    }

    /**
     * Gets the requested Object by path.
     *
     * @param path Path of the Object to get
     * @return Requested Object
     */
    @Nullable
    protected Object get(@NotNull String path) {
        Object value = getConfig().get(path);
        if (!(value instanceof ConfigurationSection section)) {
            return value;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            String rawValue = section.getString(key);
            if (rawValue == null) {
                continue;
            }
            map.put(key, string2Object(rawValue));
        }
        return map;
    }

    /**
     * Converts a string representation of an object into an Object.
     *
     * @param rawValue Raw String
     * @return New Object
     */
    @NotNull
    protected Object string2Object(@NotNull String rawValue) {
        return rawValue;
    }

    /**
     * Sets the comment at the specified path.
     * <p>
     * If value is null, the comment will be removed. If the path does
     * not exist, no comment will be set. Any existing comment will be
     * replaced, regardless of what the new comment is.
     * <p>
     * Use \n for newline.
     *
     * @param path    Path of the comment to set
     * @param comment New comment to set at the path
     */
    protected void setComment(@NotNull String path, @Nullable String comment) {
        getConfig().setComment(path, comment, CommentType.BLOCK);
    }

    /**
     * Key for a YAML element.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Key {
        /**
         * Get value of this key.
         *
         * @return Key value
         */
        String value();
    }

    /**
     * Comment of a YAML element.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Comment {
        /**
         * Get value of this comment.
         *
         * @return Comment value
         */
        String value();
    }
}
