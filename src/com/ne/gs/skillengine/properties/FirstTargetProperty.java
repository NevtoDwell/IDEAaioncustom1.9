/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.properties;

import com.ne.gs.model.ChatType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.model.DispelCategoryType;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public final class FirstTargetProperty {

    /**
     * @param skill
     * @param properties
     *
     * @return
     */
    public static boolean set(Skill skill, Properties properties) {

        FirstTargetAttribute value = properties.getFirstTarget();
        skill.setFirstTargetAttribute(value);
        switch (value) {
            case ME:
                skill.setFirstTargetRangeCheck(false);
                skill.setFirstTarget(skill.getEffector());
                break;
            case TARGETORME:
                boolean changeTargetToMe = false;
                if (skill.getFirstTarget() == null) {
                    skill.setFirstTarget(skill.getEffector());
                } else if (skill.getFirstTarget().isAttackableNpc()) {
                    Player playerEffector = (Player) skill.getEffector();
                    if (skill.getFirstTarget().isEnemy(playerEffector)) {
                        changeTargetToMe = true;
                    }
                } else if ((skill.getFirstTarget() instanceof Player) && (skill.getEffector() instanceof Player)) {
                    Player playerEffected = (Player) skill.getFirstTarget();
                    Player playerEffector = (Player) skill.getEffector();
                    if (!playerEffected.getRace().equals(playerEffector.getRace()) || playerEffected.isEnemy(playerEffector)) {
                        changeTargetToMe = true;
                    }
                } else if (skill.getFirstTarget() instanceof Npc) {
                    Npc npcEffected = (Npc) skill.getFirstTarget();
                    Player playerEffector = (Player) skill.getEffector();
                    if (npcEffected.isEnemy(playerEffector)) {
                        changeTargetToMe = true;
                    }
                } else if ((skill.getFirstTarget() instanceof Summon) && (skill.getEffector() instanceof Player)) {
                    Summon summon = (Summon) skill.getFirstTarget();
                    Player playerEffected = summon.getMaster();
                    Player playerEffector = (Player) skill.getEffector();
                    if (playerEffected.isEnemy(playerEffector)) {
                        changeTargetToMe = true;
                    }
                }
                if (changeTargetToMe) {
                    if (skill.getEffector() instanceof Player) {
                        ((Player) skill.getEffector()).sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_AUTO_CHANGE_TARGET_TO_MY);
                    }

                    skill.setFirstTarget(skill.getEffector());
                }
                break;
            case TARGET:
                if (skill.getEffector() instanceof Player) {
                    Player player = (Player) skill.getEffector();
                    if (skill.getSkillId() == 2369 && player.getTarget() != null
                            && player.getTarget() == player) {
                        //1300088
                        player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID);
                        return false;
                    }
                }
                if ((skill.getSkillId() <= 8000) || (skill.getSkillId() >= 9000)) {
                    if ((skill.getSkillTemplate().getDispelCategory() != DispelCategoryType.NPC_BUFF) && (skill.getSkillTemplate().getDispelCategory() != DispelCategoryType.NPC_DEBUFF_PHYSICAL)) {
                        if (((skill.getFirstTarget() == null) || (skill.getFirstTarget().equals(skill.getEffector()))) && ((skill.getEffector() instanceof Player))) {
                            if (skill.getSkillTemplate().getProperties().getTargetType() == TargetRangeAttribute.AREA) {
                                return skill.getFirstTarget() != null;
                            }
                            TargetRelationAttribute relation = skill.getSkillTemplate().getProperties().getTargetRelation();
                            if (relation != TargetRelationAttribute.ALL) {
                                ((Player) skill.getEffector()).sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID);
                                return false;
                            }
                        }
                    }
                }
                break;
            case MYPET:
                Creature effector = skill.getEffector();
                if (effector instanceof Player) {
                    Summon summon = ((Player) effector).getSummon();
                    if (summon != null) {
                        skill.setFirstTarget(summon);
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
                break;
            case MYMASTER:
                Creature peteffector = skill.getEffector();
                if (peteffector instanceof Summon) {
                    Player player = ((Summon) peteffector).getMaster();
                    if (player != null) {
                        skill.setFirstTarget(player);
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
                break;
            case PASSIVE:
                skill.setFirstTarget(skill.getEffector());
                break;
            case TARGET_MYPARTY_NONVISIBLE:
                Creature effected = skill.getFirstTarget();
                if ((effected == null) || (skill.getEffector() == null)) {
                    return false;
                }
                if ((!(effected instanceof Player)) || (!(skill.getEffector() instanceof Player)) || (!((Player) skill.getEffector()).isInGroup2())) {
                    return false;
                }
                boolean myParty = false;
                for (Player member : ((Player) skill.getEffector()).getPlayerGroup2().getMembers()) {
                    if (member != skill.getEffector()) {
                        if (member == effected) {
                            myParty = true;
                            break;
                        }
                    }
                }
                if (!myParty) {
                    return false;
                }
                skill.setFirstTargetRangeCheck(false);
                break;
            case POINT:
                skill.setFirstTarget(skill.getEffector());
                skill.setFirstTargetRangeCheck(false);
                return true;
        }

        if (skill.getFirstTarget() != null) {
            skill.getEffectedList().add(skill.getFirstTarget());
        }
        return true;
    }
}
