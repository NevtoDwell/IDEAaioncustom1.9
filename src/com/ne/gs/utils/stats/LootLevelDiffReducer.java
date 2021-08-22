/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.common.primitives.Ints;

import com.ne.gs.configs.main.DropConfig;

/**
 * @author hex1r0
 */
public final class LootLevelDiffReducer {

    private static String _cachedProperty = "";
    private static E[] _cache;

    /**
     * @param levelDiff
     *         difference between to objects
     *
     * @return reduce percent
     */
    public static int of(int levelDiff) {
        levelDiff = Math.abs(levelDiff);

        if (!_cachedProperty.equals(DropConfig.DROP_REDUCTION)) {
            synchronized (LootLevelDiffReducer.class) {
                List<E> tmp = new ArrayList<>();
                String[] entries = DropConfig.DROP_REDUCTION.split(";");
                for (String entry : entries) {
                    String[] vals = entry.split(":");
                    tmp.add(new E(Integer.parseInt(vals[0]), Integer.parseInt(vals[1])));
                }

                Collections.sort(tmp);

                _cache = tmp.toArray(new E[tmp.size()]);
                _cachedProperty = DropConfig.DROP_REDUCTION;
            }
        }

        for (E e : _cache) {
            if (e.level <= levelDiff) {
                return e.mod;
            }
        }

        return 100;
    }

    private static class E implements Comparable<E> {
        final int level;
        final int mod;

        private E(int level, int mod) {
            this.level = level;
            this.mod = mod;
        }

        @Override
        public int compareTo(E o) {
            return Ints.compare(o.level, level);
        }

        @Override
        public String toString() {
            return String.format("E{level=%d, mod=%d}", level, mod);
        }
    }
}
