/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.collections.cachemap;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a simple map implementation for cache usage.<br>
 * <br>
 * Values from the map will be removed after the first garbage collector run if there isn't any strong reference to the value object.
 *
 * @author Luno
 */
class WeakCacheMap<K, V> extends AbstractCacheMap<K, V> implements CacheMap<K, V> {

    private static final Logger log = LoggerFactory.getLogger(WeakCacheMap.class);

    /**
     * This class is a {@link WeakReference} with additional responsibility of holding key object
     *
     * @author Luno
     */
    private class Entry extends WeakReference<V> {

        private final K key;

        Entry(K key, V referent, ReferenceQueue<? super V> q) {
            super(referent, q);
            this.key = key;
        }

        K getKey() {
            return key;
        }
    }

    WeakCacheMap(String cacheName, String valueName) {
        super(cacheName, valueName, log);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected synchronized void cleanQueue() {
        Entry en = null;
        while ((en = (Entry) refQueue.poll()) != null) {
            K key = en.getKey();
            if (log.isDebugEnabled()) {
                log.debug(cacheName + " : cleaned up " + valueName + " for key: " + key);
            }
            cacheMap.remove(key);
        }
    }

    @Override
    protected Reference<V> newReference(K key, V value, ReferenceQueue<V> vReferenceQueue) {
        return new Entry(key, value, vReferenceQueue);
    }
}
