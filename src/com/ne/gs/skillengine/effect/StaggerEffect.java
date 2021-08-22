/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import com.ne.gs.controllers.movement.PlayerMoveController;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_FORCED_MOVE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillMoveType;
import com.ne.gs.skillengine.model.SpellStatus;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.GeoHelper;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.math.Vector3f;
import mw.utils.GeomUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StaggerEffect")
public class StaggerEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
        // если цель в оковах(воздушных оковах) или опрокинута отталкиания не будет
        if (effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.STUMBLE)
                || effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.ROOT)
                || effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL)) {
            return;
        }
        final Creature effected = effect.getEffected();
        //effected.getEffectController().removeParalyzeEffects();
        effected.getMoveController().abortMove();
        PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(),
                new SM_FORCED_MOVE(effect.getEffector(), effect.getEffected().getObjectId(), effect.getTargetX(), effect.getTargetY(), effect.getTargetZ()));


        if (effected instanceof Player)
            ((PlayerMoveController) effected.getMoveController()).setBegin(effect.getTargetX(), effect.getTargetY(), effect.getTargetZ());

        World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(),
                effected.getHeading());

    }

    @Override
    public void startEffect(Effect effect) {
        effect.getEffected().getEffectController().setAbnormal(AbnormalState.STAGGER.getId());
        effect.setAbnormal(AbnormalState.STAGGER.getId());
        effect.getEffected().getEffectController().removeParalyzeEffects();
    }

    @Override
    public void calculate(Effect effect) {
        if (!super.calculate(effect, StatEnum.STAGGER_RESISTANCE, SpellStatus.STAGGER))
            return;

        // Check for packets if this must be fixed someway, but for now it works good so
        effect.setSkillMoveType(SkillMoveType.STAGGER);
        final Creature effector = effect.getEffector();
        final Creature effected = effect.getEffected();
        effected.getController().cancelCurrentSkill();
        // Move effected 3 meters backward as on retail
        double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effector.getHeading()));
        float x1 = (float) (Math.cos(radian) * 3);
        float y1 = (float) (Math.sin(radian) * 3);

        byte intentions = (byte) (CollidableType.PHYSICAL.getId() | CollidableType.DOOR.getId());
        Vector3f target = GeoEngine.getAvailablePoint(effected, x1, y1, intentions);

        effect.setTargetLoc(target.x, target.y, target.z);
    }

    @Override
    public void endEffect(Effect effect) {
        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.STAGGER.getId());
    }
}