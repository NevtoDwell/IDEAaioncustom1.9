/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.controllers.movement.PlayerMoveController;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_FORCED_MOVE;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_IMMOBILIZE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillMoveType;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;


/**
 * @author Sarynth modified by Wakizashi, Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PulledEffect")
public class PulledEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
        final Creature effected = effect.getEffected();
        effected.setCriticalEffectMulti(0);
        effected.getController().cancelCurrentSkill();
        //effected.getMoveController().abortMove();
        World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(), effected.getHeading());

        if (effected instanceof Player)
            ((PlayerMoveController) effected.getMoveController()).setBegin(effect.getTargetX(), effect.getTargetY(), effect.getTargetZ());

        PacketSendUtility.broadcastPacketAndReceive(effected, new SM_FORCED_MOVE(effect.getEffector(), effected.getObjectId(), effect.getTargetX(), effect.getTargetY(), effect.getTargetZ()));
    }

    @Override
    public void calculate(Effect effect) {
        Creature effected = effect.getEffected();

        if (effected == null) {
            return;
        }
        if (effected.getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL) ||
                effected.getEffectController().isAbnormalSet(AbnormalState.STUMBLE)) {
            return;
        }
        if (!super.calculate(effect, StatEnum.PULLED_RESISTANCE, null)) {
            return;
        }

        effect.setSkillMoveType(SkillMoveType.PULL);
        Creature effector = effect.getEffector();

        // Target must be pulled just one meter away from effector, not IN place of effector
        double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effector.getHeading()));
        float x1 = (float) Math.cos(radian);
        float y1 = (float) Math.sin(radian);


        float x, y, z;
        x = effector.getX();
        y = effector.getY();
        z = effector.getZ();

        effect.setTargetLoc(x + x1, y + y1, z + 0.25F);
    }

    @Override
    public void startEffect(Effect effect) {
        Creature effected = effect.getEffected();
		
        //fix снятия стойки
        if (effected instanceof Player) {
            Player playerEffector = (Player) effected;
            if (playerEffector.getController().isUnderStance()) {
                playerEffector.getController().stopStance();
            }
        }
		
        effected.getEffectController().setAbnormal(AbnormalState.CANNOT_MOVE.getId());
        effect.setAbnormal(AbnormalState.CANNOT_MOVE.getId());
        PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_TARGET_IMMOBILIZE(effect.getEffected()));
    }

    @Override
    public void endEffect(Effect effect) {
        effect.setIsPhysicalState(false);
        effect.getEffected().setCriticalEffectMulti(1);
        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.CANNOT_MOVE.getId());
    }
}
