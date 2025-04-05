/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.bus;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import org.jetbrains.annotations.Nullable;

/*
 * Helper class that should be faster then ConcurrentHashMap,
 * yet still manages to properly deal with many threads.
 */
public class LockHelper<K, V> {
    public static <K, V> LockHelper<K, V> withHashMap() {
        // convert size to capacity according to default load factor
        return new LockHelper<>(size -> new HashMap<>((size + 2) * 4 / 3));
    }

    public static <K, V> LockHelper<K, V> withIdentityHashMap() {
        return new LockHelper<>(IdentityHashMap::new);
    }

    private final IntFunction<Map<K, V>> mapConstructor;
    /**
     * Only modify this map while holding the lock object!
     */
    private final Map<K, V> backingMap;
    @Nullable
    private volatile Map<K, V> readOnlyView = null;
    private Object lock = new Object();

    private LockHelper(IntFunction<Map<K, V>> mapConstructor) {
        this.mapConstructor = mapConstructor;
        this.backingMap = mapConstructor.apply(32); // reasonable initial size
    }

    Map<K, V> getReadMap() {
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

    public V computeIfAbsent(K key, Function<K, V> factory) {
        return computeIfAbsent(key, factory, Function.identity());
    }

    public <I> V computeIfAbsent(K key, Function<K, I> factory, Function<I, V> finalizer) {
        // Try lock-free get first
        var ret = get(key);
        if (ret != null)
            return ret;

        // Let's pre-compute our new value. This could take a while, as well as recursively call this
        // function. as such, we need to make sure we don't hold a lock when we do this, otherwise
        // we could conflict with the class init global lock that is implicitly present
        var intermediate = factory.apply(key);

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

    public void clearAll() {
        backingMap.clear();
        readOnlyView = null;
        lock = new Object();
    }
}
