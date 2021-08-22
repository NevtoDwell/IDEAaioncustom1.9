/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect.modifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer modified by Sippolo, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetRaceDamageModifier")
public class TargetRaceDamageModifier extends ActionModifier {

    @XmlAttribute(name = "race")
    private Race skillTargetRace;

    @Override
    public int analyze(Effect effect) {
        Creature effected = effect.getEffected();

        int newValue = (value + effect.getSkillLevel() * delta);
        if (effected instanceof Player) {

            Player player = (Player) effected;
            switch (skillTargetRace) {
                case ASMODIANS:
                    if (player.getRace() == Race.ASMODIANS) {
                        return newValue;
                    }
                    break;
                case ELYOS:
                    if (player.getRace() == Race.ELYOS) {
                        return newValue;
                    }
            }
        } else if (effected instanceof Npc) {
            Npc npc = (Npc) effected;
            if (skillTargetRace == npc.getRace()) {
                return newValue;
            } else {
                return 0;
            }
        }

        return 0;
    }

    @Override
    public boolean check(Effect effect) {
        Creature effected = effect.getEffected();
        if (effected instanceof Player) {

            Player player = (Player) effected;
            Race race = player.getRace();
            return race == Race.ASMODIANS && skillTargetRace == Race.ASMODIANS || race == Race.ELYOS && skillTargetRace == Race.ELYOS;
        } else if (effected instanceof Npc) {
            Npc npc = (Npc) effected;

        return skillTargetRace == npc.getRace();
        }

        return false;
    }

}
