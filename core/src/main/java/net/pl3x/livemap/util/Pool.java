package net.pl3x.livemap.util;

import io.undertow.util.FastConcurrentDirectDeque;
import java.util.Deque;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a simple pool for reusable objects.
 *
 * @param <T> Type of object pooled in this pool
 */
public class Pool<T extends Pool.Reusable> {
    private final Deque<T> pool = new FastConcurrentDirectDeque<>();

    private final Supplier<T> supplier;

    /**
     * Constructs a new instance of Pool.
     *
     * @param supplier Supplier for creating a new reusable objects
     */
    public Pool(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Get a reusable object from the pool.
     * <p>
     * Will allocate and instantiate a new object if none are available in the pool.
     *
     * @return Requested reusable object
     */
    @NotNull
    public T get() {
        T obj = this.pool.poll();
        if (obj == null) {
            return this.supplier.get();
        }
        return obj;
    }

    /**
     * Put an unused object back into the pool.
     *
     * @param obj Unused reusable object
     */
    public void put(@NotNull T obj) {
        this.pool.add(obj);
    }

    /**
     * Clear all objects from this pool.
     */
    public void clear() {
        this.pool.clear();
    }

    /**
     * Interface for reusable objects that can be inserted into this pool.
     */
    public interface Reusable {
    }
}
