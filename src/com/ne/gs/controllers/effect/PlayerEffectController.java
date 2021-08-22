/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.effect;

import java.util.Collection;
import java.util.Collections;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_ABNORMAL_STATE;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_STANCE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTargetSlot;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.taskmanager.tasks.PacketBroadcaster.BroadcastMode;
import com.ne.gs.taskmanager.tasks.TeamEffectUpdater;

/**
 * @author ATracer
 */
public class PlayerEffectController extends EffectController {

    public PlayerEffectController(Creature owner) {
        super(owner);
    }

    @Override
    public void addEffect(Effect effect) {
        if (checkDuelCondition(effect) && !effect.getIsForcedEffect()) {
            return;
        }

        super.addEffect(effect);
        updatePlayerIconsAndGroup(effect);
    }

    @Override
    public void clearEffect(Effect effect) {
        super.clearEffect(effect);
        updatePlayerIconsAndGroup(effect);
    }

    @Override
    public Player getOwner() {
        return (Player) super.getOwner();
    }

    /**
     * @param effect
     */
    private void updatePlayerIconsAndGroup(Effect effect) {
        if (!effect.isPassive()) {
            updatePlayerEffectIcons();
            if (getOwner().isInTeam()) {
                TeamEffectUpdater.getInstance().startTask(getOwner());
            }
        }
    }

    @Override
    public void updatePlayerEffectIcons() {
        getOwner().addPacketBroadcastMask(BroadcastMode.UPDATE_PLAYER_EFFECT_ICONS);
    }

    @Override
    public void updatePlayerEffectIconsImpl() {
        Collection<Effect> effects = getAbnormalEffectsToShow();
        getOwner().sendPck(new SM_ABNORMAL_STATE(effects, abnormals));
    }

    /**
     * Effect of DEBUFF should not be added if duel ended (friendly unit)
     *
     * @param effect
     *
     * @return
     */
    private boolean checkDuelCondition(Effect effect) {
        Creature creature = effect.getEffector();
        if (creature instanceof Player) {
            if (!getOwner().isEnemy(creature) && effect.getTargetSlot() == SkillTargetSlot.DEBUFF.ordinal()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param skillId
     * @param skillLvl
     */
    public void addSavedEffect(int skillId, int skillLvl, int remainingTime, long endTime) {
        SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);

        if (remainingTime <= 0) {
            return;
        }
        if (CustomConfig.ABYSSXFORM_LOGOUT && template.isDeityAvatar()) {
            if (System.currentTimeMillis() >= endTime) {
                return;
            } else {
                remainingTime = (int) (endTime - System.currentTimeMillis());
            }
        }

        Effect effect = new Effect(getOwner(), getOwner(), template, skillLvl, remainingTime);
        abnormalEffectMap.put(effect.getStack(), effect);
        effect.addAllEffectToSucess();
        effect.startEffect(true);

        if (effect.getSkillTemplate().getTargetSlot() != SkillTargetSlot.NOSHOW) {
            getOwner().sendPck(new SM_ABNORMAL_STATE(Collections.singletonList(effect), abnormals));
        }

    }

    @Override
    public void broadCastEffectsImp() {
        super.broadCastEffectsImp();
        Player player = getOwner();
        if (player.getController().isUnderStance()) {
            player.sendPck(new SM_PLAYER_STANCE(player, 1));
        }
    }

}
