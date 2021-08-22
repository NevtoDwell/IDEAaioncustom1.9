/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;
import java.util.Map;
import com.google.common.base.Function;

import com.ne.commons.Sys;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.func.tuple.Tuple3;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

import static com.google.common.collect.Collections2.transform;

/**
 * @author hex1r0
 */
public class SM_ITEM_COOLDOWN extends AionServerPacket {
    private final Collection<Tuple3<Integer, Integer, Integer>> _entries;

    public SM_ITEM_COOLDOWN(Map<Integer, Tuple2<Long, Integer>> cooldowns) {
        final long now = Sys.millis();
        _entries = transform(cooldowns.entrySet(),
            new Function<Map.Entry<Integer, Tuple2<Long, Integer>>, Tuple3<Integer, Integer, Integer>>() {
                @Override
                public Tuple3<Integer, Integer, Integer> apply(Map.Entry<Integer, Tuple2<Long, Integer>> e) {
                    int left = Math.max(0, (int) ((e.getValue()._1 - now) / 1000));
                    return Tuple3.of(e.getKey(), left, e.getValue()._2);
                }
            });
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeH(_entries.size());
        for (Tuple3<Integer, Integer, Integer> e : _entries) {
            writeH(e._1);
            writeD(e._2);
            writeD(e._3);
        }
    }
}
