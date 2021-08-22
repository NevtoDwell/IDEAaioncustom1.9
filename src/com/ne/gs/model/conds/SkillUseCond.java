/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.conds;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.SimpleCond;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.restrictions.PlayerRestrictions;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.effect.EffectType;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.skillengine.model.SkillType;
import com.ne.gs.skillengine.model.TransformType;

/**
 * @author hex1r0
 */
public abstract class SkillUseCond extends SimpleCond<Tuple2<Player, Skill>> {

    public static final SkillUseCond STATIC = new SkillUseCond() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Skill> e) {
            Player player = e._1;
            Skill skill = e._2;
            VisibleObject target = player.getTarget();
            SkillTemplate tpl = skill.getSkillTemplate();

            if (!PlayerRestrictions.checkFly(player, target)) {
                return false;
            }

            // check if is casting to avoid multicast exploit
            // TODO cancel skill if other is used
            if (player.isCasting()) {
                return false;
            }

            if (!player.canAttack() && !tpl.hasEvadeEffect()) {
                return false;
            }

            int stigmaId = tpl.getStigmaId();
            if (stigmaId != 0) {
                if (player.getEquipment().getEquippedItemsByItemId(stigmaId).isEmpty()) {
                    if (player.isGM()) {
                        player.sendMsg("GM Only: Skipping stigma " + stigmaId + " check");
                    } else {
                        return false;
                    }
                }
            }

            if ((tpl.getType() == SkillType.MAGICAL) && (player.getEffectController().isAbnormalSet(AbnormalState.SILENCE))
                //&& (!tpl.hasEvadeEffect()) - (снятие шока) удалить
                ) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_MAGIC_SKILL_WHILE_SILENCED);
                return false;
            }

            if ((tpl.getType() == SkillType.PHYSICAL) && (player.getEffectController().isAbnormalSet(AbnormalState.BIND))) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_PHYSICAL_SKILL_IN_FEAR);
                return false;
            }

            if (player.isSkillDisabled(tpl)) {
                return false;
            }

            // cannot use skills while transformed
            if (player.getTransformModel().isActive() && player.getTransformModel().getType() == TransformType.NONE) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_CAST_IN_SHAPECHANGE);
                return false;
            }

            if (tpl.hasResurrectEffect()) {
                if (!(target instanceof Player)) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID);
                    return false;
                }
                Player targetPlayer = (Player) target;
                if (!targetPlayer.isInState(CreatureState.DEAD) && !targetPlayer.getLifeStats().isAlreadyDead()) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID);
                    return false;
                }
            }

            return true;
        }
    };

    @NotNull
    @Override
    public final String getType() {
        return SkillUseCond.class.getName();
    }
}
