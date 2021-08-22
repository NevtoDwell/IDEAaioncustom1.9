/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import com.ne.commons.Sys;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author hex1r0
 */
public class SM_SKILL_COOLDOWN extends AionServerPacket {
    private final List<Tuple2<Integer, Integer>> _entries;

    public SM_SKILL_COOLDOWN(Map<Integer, Long> cooldowns) {
        final long now = Sys.millis();
        _entries = FluentIterable
            .from(cooldowns.entrySet())
            .transformAndConcat(new Function<Map.Entry<Integer, Long>, Iterable<Tuple2<Integer, Integer>>>() {
                @Override
                public Iterable<Tuple2<Integer, Integer>> apply(Map.Entry<Integer, Long> e) {
                    List<Integer> skills = DataManager.SKILL_DATA.getSkillsForCooldownId(e.getKey());
                    List<Tuple2<Integer, Integer>> out = new ArrayList<>(skills.size());
                    int left = Math.max(0, (int) ((e.getValue() - now) / 1000));
                    for (Integer skillId : skills) {
                        out.add(Tuple2.of(skillId, left));
                    }
                    return out;
                }
            }).toList();
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeH(_entries.size());
        for (Tuple2<Integer, Integer> e : _entries) {
            writeH(e._1);
            writeD(e._2);
        }
    }
}
