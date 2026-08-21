package net.pl3x.livemap.util;

import java.util.HashMap;

/**
 * Represents a registry of key-value pairs.
 *
 * @param <T> Type of registry
 */
public abstract class Registry<T> extends HashMap<String, T> {
    /**
     * Constructs a new instance of Registry.
     */
    public Registry() {
        // Explicit constructor to satisfy Javadoc and linter tools
    }

    /**
     * Rebuilds the registry.
     */
    public abstract void rebuild();
}
