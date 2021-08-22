/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.collections;

@SuppressWarnings({"rawtypes"})
public interface ICache<K extends Comparable, V> {

    V get(K obj);

    void put(K key, V obj);

    void remove(K key);

    CachePair[] getAll();

    int size();
}
