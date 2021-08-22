/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.properties;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.templates.npc.AbyssNpcType;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.properties.Properties.CastState;
import com.ne.gs.utils.MathUtil;
import mw.engines.geo.GeoHelper;

/**
 * @author ATracer
 */
public final class FirstTargetRangeProperty {

    /**
     * @param skill
     * @param properties
     */
    public static boolean set(Skill skill, Properties properties, CastState castState) {
        float firstTargetRange = properties.getFirstTargetRange();
        if (!skill.isFirstTargetRangeCheck()) {
            return true;
        }

        Creature effector = skill.getEffector();
        Creature firstTarget = skill.getFirstTarget();

        if (firstTarget == null) {
            return false;
        }

        // Add Weapon Range to distance
        if (properties.isAddWeaponRange()) {
            firstTargetRange += (float) skill.getEffector().getGameStats().getAttackRange().getCurrent() / 1000f;
        }

        // on end cast check add revision distance value
        if (!castState.isCastStart()) {
            firstTargetRange += properties.getRevisionDistance();
        }

        if ((int) firstTarget.getObjectId() == effector.getObjectId()) {
            return true;
        }

        if (!MathUtil.isInAttackRange(effector, firstTarget, firstTargetRange + 2)) {
            if (effector instanceof Player) {
                ((Player) effector).sendPck(SM_SYSTEM_MESSAGE.STR_ATTACK_TOO_FAR_FROM_TARGET);
            }
            return false;
        }

        // TODO check for all targets too
        // Summon Group Member exception
        if (skill.getSkillTemplate().getSkillId() != 1606) {

            //MW FIXME Temporary solution to pass abyss gate attacks
            if(!(firstTarget instanceof SiegeNpc && ((SiegeNpc)firstTarget).getObjectTemplate().getAbyssNpcType() == AbyssNpcType.DOOR)) {
                if (!GeoHelper.canSee(effector, firstTarget)) {
                    if (effector instanceof Player) {
                        ((Player) effector).sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_OBSTACLE);
                    }
                    return false;
                }
            }
        }
        return true;
    }

}
