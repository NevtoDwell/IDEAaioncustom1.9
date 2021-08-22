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

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Kisk;
import com.ne.gs.model.gameobjects.Servant;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
public final class TargetRelationProperty {

    /**
     * @param skill
     * @param properties
     *
     * @return
     */
    public static boolean set(Skill skill, Properties properties) {

        TargetRelationAttribute value = properties.getTargetRelation();

        List<Creature> effectedList = skill.getEffectedList();
        boolean isMaterialSkill = DataManager.MATERIAL_DATA.isMaterialSkill(skill.getSkillId());
        Creature effector = skill.getEffector();

        switch (value) {
            case ALL:
                break;
            case ENEMY:
                for (Iterator<Creature> iter = effectedList.iterator(); iter.hasNext(); ) {
                    Creature nextEffected = iter.next();

                    if (effector.isEnemy(nextEffected)) {
                        continue;
                    }

                    if (
                            isMaterialSkill &&
                            !(nextEffected instanceof Kisk && nextEffected.isInsideZoneType(ZoneType.NEUTRAL))) {
                        
                        continue;
                    }

                    iter.remove();
                }
                break;
            case FRIEND:
                for (Iterator<Creature> iter = effectedList.iterator(); iter.hasNext(); ) {
                    Creature nextEffected = iter.next();

                    if (!effector.isEnemy(nextEffected) && isBuffAllowed(nextEffected) || isMaterialSkill) {
                        continue;
                    }

                    iter.remove();
                }

                if (effectedList.isEmpty()) {
                    skill.setFirstTarget(skill.getEffector());
                    effectedList.add(skill.getEffector());
                } else {
                    skill.setFirstTarget(effectedList.get(0));
                }
                break;
            case MYPARTY:
                for (Iterator<Creature> iter = effectedList.iterator(); iter.hasNext(); ) {
                    Creature nextEffected = iter.next();

                    Player player = null;
                    if (nextEffected instanceof Player) {
                        player = (Player) nextEffected;
                    } else if (nextEffected instanceof Summon) {
                        Summon playerSummon = (Summon) nextEffected;
                        if (playerSummon.getMaster() != null) {
                            player = playerSummon.getMaster();
                        }
                    }
                    if (player != null) {
                        if (effector instanceof Servant) {
                            effector = effector.getMaster();
                        }

                        Player playerEffector = (Player) effector;
                        if (playerEffector.isInAlliance2() && player.isInAlliance2()) {
                            if (playerEffector.getPlayerAlliance2().getObjectId().equals(player.getPlayerAlliance2().getObjectId())) {
                                continue;
                            }
                        } else if (playerEffector.isInGroup2() && player.isInGroup2()) {
                            if (playerEffector.getPlayerGroup2().getTeamId().equals(player.getPlayerGroup2().getTeamId())) {
                                continue;
                            }
                        }
                    }
                    iter.remove();
                }

                if (effectedList.isEmpty()) {
                    skill.setFirstTarget(effector);
                    effectedList.add(effector);
                } else {
                    skill.setFirstTarget(effectedList.get(0));
                }
                break;
        }

        return true;
    }

    /**
     * @return true = allow buff, false = deny buff
     */
    public static boolean isBuffAllowed(Creature effected) {
        if ((effected instanceof SiegeNpc)) {
            switch (((SiegeNpc) effected).getObjectTemplate().getAbyssNpcType()) {
                case ARTIFACT:
                case ARTIFACT_EFFECT_CORE:
                case DOOR:
                case DOORREPAIR:
                    return false;
            }
        }
        return true;
    }
}
