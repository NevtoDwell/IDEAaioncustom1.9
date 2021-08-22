/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.collections.cachemap;

/**
 * @author Luno
 */
public final class CacheMapFactory {

    /**
     * Creates and returns an instance of {@link SoftCacheMap}
     *
     * @param <K>
     *     - Type of keys
     * @param <V>
     *     - Type of values
     * @param cacheName
     *     - The name for this cache map
     * @param valueName
     *     - Mnemonic name for values stored in the cache
     *
     * @return CacheMap<K, V>
     */
    public static <K, V> CacheMap<K, V> createSoftCacheMap(String cacheName, String valueName) {
        return new SoftCacheMap<>(cacheName, valueName);
    }

    /**
     * Creates and returns an instance of {@link WeakCacheMap}
     *
     * @param <K>
     *     - Type of keys
     * @param <V>
     *     - Type of values
     * @param cacheName
     *     - The name for this cache map
     * @param valueName
     *     - Mnemonic name for values stored in the cache
     *
     * @return CacheMap<K, V>
     */
    public static <K, V> CacheMap<K, V> createWeakCacheMap(String cacheName, String valueName) {
        return new WeakCacheMap<>(cacheName, valueName);
    }
}
