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
import com.ne.gs.network.aion.serverpackets.SM_TARGET_IMMOBILIZE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillMoveType;
import com.ne.gs.skillengine.model.SpellStatus;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.PositionUtil;
import com.ne.gs.world.World;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.GeoHelper;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.math.Vector3f;
import mw.utils.GeomUtil;

/**
 * @author VladimirZ
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleRootEffect")
public class SimpleRootEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
        Creature effected = effect.getEffected();

        if (effected instanceof Player)
            ((PlayerMoveController) effected.getMoveController()).setBegin(effect.getTargetX(), effect.getTargetY(), effect.getTargetZ());

        World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(), effected.getHeading());
    }

    @Override
    public void calculate(Effect effect) {
        // Effect is applied only on moving targets, REALLY?
        if (!effect.getEffected().getMoveController().isInMove()) {
            return;
        }

        if (!super.calculate(effect, StatEnum.STAGGER_RESISTANCE, null)) {
            return;
        }

        Creature effected = effect.getEffected();

        effect.setSpellStatus(SpellStatus.NONE);
        effect.setSkillMoveType(SkillMoveType.KNOCKBACK);

        double radian = PositionUtil.isBehindTarget(effect.getEffector(), effect.getEffected())
                ? Math.atan2(effect.getEffector().getY() - effect.getEffected().getY(), effect.getEffector().getX() - effect.getEffected().getX())
                : Math.toRadians(MathUtil.convertHeadingToDegree(effect.getEffector().getHeading()));

        float x1 = (float) (Math.cos(radian) * 1);
        float y1 = (float) (Math.sin(radian) * 1);

        float z = effected.getZ();
        byte intentions = (byte) (CollidableType.PHYSICAL.getId() | CollidableType.DOOR.getId());

        Vector3f closestCollision =  GeoEngine.getAvailablePoint(effected, x1, y1, intentions);

        x1 = closestCollision.x;
        y1 = closestCollision.y;
        z = closestCollision.z;
        effect.setTargetLoc(x1, y1, z);
    }


    @Override
    public void startEffect(Effect effect) {
        // effect.getEffected().getController().cancelCurrentSkill(); //TODO: Not sure about this
        effect.getEffected().getEffectController().setAbnormal(AbnormalState.KNOCKBACK.getId());
        effect.setAbnormal(AbnormalState.KNOCKBACK.getId());
        PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_TARGET_IMMOBILIZE(effect.getEffected()));
    }

    @Override
    public void endEffect(Effect effect) {
        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.KNOCKBACK.getId());
    }
}
