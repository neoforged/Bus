package net.minecraftforge.eventbus;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/*
 *
 * Helper class that should be faster then ConcurrentHashMap,
 * yet still manages to properly deal with many threads.
 */
public class LockHelper<K,V> {
    private final IntFunction<Map<K, V>> mapConstructor;
    /**
     * Only modify this map while holding the lock object!
     */
    private final Map<K, V> backingMap;
    @Nullable
    private volatile Map<K, V> readOnlyView = null;
    private Object lock = new Object();

    public LockHelper(IntFunction<Map<K, V>> mapConstructor) {
        this.mapConstructor = mapConstructor;
        this.backingMap = mapConstructor.apply(0);
    }

    private Map<K, V> getReadMap() {
        var map = readOnlyView;
        if (map == null) {
            // Need to update the read map
            synchronized (lock) {
                var updatedMap = mapConstructor.apply(backingMap.size());
                updatedMap.putAll(backingMap);
                readOnlyView = map = updatedMap;
            }
        }
        return map;
    }

    public V get(K key) {
        return getReadMap().get(key);
    }

    public boolean containsKey(K key) {
        return getReadMap().containsKey(key);
    }

    public V computeIfAbsent(K key, Supplier<V> factory) {
        return computeIfAbsent(key, factory, Function.identity());
    }

    @Deprecated(forRemoval = true, since = "6.0") // I chose a stupid name, it should be computeIfAbsent
    public <I> V get(K key, Supplier<I> factory, Function<I, V> finalizer) {
        return computeIfAbsent(key, factory, finalizer);
    }

    public <I> V computeIfAbsent(K key, Supplier<I> factory, Function<I, V> finalizer) {
        // Try lock-free get first
        var ret = get(key);
        if (ret != null)
            return ret;

        // Let's pre-compute our new value. This could take a while, as well as recursively call this
        // function. as such, we need to make sure we don't hold a lock when we do this, otherwise
        // we could conflict with the class init global lock that is implicitly present
        var intermediate = factory.get();

        // having computed a value, we'll grab the lock.
        synchronized (lock) {
            // Check if some other thread already created a value
            ret = backingMap.get(key);
            if (ret == null) {
                // Run any finalization we need, this was added because ClassLoaderFactory will actually define the class here
                ret = finalizer.apply(intermediate);
                // Update the map
                backingMap.put(key, ret);
                readOnlyView = null;
            }
        }

        return ret;
    }

    // TODO: it's unclear why this method even exists
    public void clearAll() {
        backingMap.clear();
        readOnlyView = null;
        lock = new Object();
    }
}
