/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.properties;

import java.util.Iterator;
import java.util.List;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Skill;

public final class TargetSpeciesProperty {

    public static boolean set(Skill skill, Properties properties) {
        TargetSpeciesAttribute value = properties.getTargetSpecies();

        List<Creature> effectedList = skill.getEffectedList();
        switch (value) {
            case NPC:
                for (Iterator<Creature> iter = effectedList.iterator(); iter.hasNext(); ) {
                    Creature nextEffected = iter.next();

                    if (!(nextEffected instanceof Npc)) {
                        iter.remove();
                    }
                }
                break;
            case PC:
                for (Iterator<Creature> iter = effectedList.iterator(); iter.hasNext(); ) {
                    Creature nextEffected = iter.next();

                    if (!(nextEffected instanceof Player)) {
                        iter.remove();
                    }
                }
        }
        return true;
    }
}
