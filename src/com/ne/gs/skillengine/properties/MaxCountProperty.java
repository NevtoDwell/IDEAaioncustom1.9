/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.properties;

import java.util.SortedMap;
import java.util.TreeMap;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.utils.MathUtil;

/**
 * @author MrPoke
 */
public final class MaxCountProperty {

    public static boolean set(Skill skill, Properties properties) {
        TargetRangeAttribute value = properties.getTargetType();
        int maxcount = properties.getTargetMaxCount();

        switch (value) {
            case AREA:
                int areaCounter = 0;
                Creature firstTarget = skill.getFirstTarget();
                if (firstTarget == null) {
                    return false;
                }
                SortedMap<Double, Creature> sortedMap = new TreeMap<>();
                for (Creature creature : skill.getEffectedList()) {
                    sortedMap.put(MathUtil.getDistance(firstTarget, creature), creature);
                }
                skill.getEffectedList().clear();
                for (Creature creature : sortedMap.values()) {
                    if (areaCounter >= maxcount) {
                        break;
                    }
                    skill.getEffectedList().add(creature);
                    areaCounter++;
                }
        }
        return true;
    }
}
