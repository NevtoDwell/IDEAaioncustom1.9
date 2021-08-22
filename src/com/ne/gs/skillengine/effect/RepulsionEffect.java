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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.controllers.movement.PlayerMoveController;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_FORCED_MOVE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;
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

/**
 * Date: 24.03.13
 * Time: 19:57
 *
 * @author Jenelli
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepulsionEffect")
public class RepulsionEffect extends EffectTemplate {
    @XmlAttribute(name = "distance")
    private float distance;

    @Override
    public void applyEffect(Effect effect) {
        Creature effected = effect.getEffected();
        effected.getController().cancelCurrentSkill();
        Skill skill = effect.getSkill();
        PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(),
                new SM_FORCED_MOVE(effect.getEffector(), effected.getObjectId(), skill.getX(), skill.getY(), skill.getZ()));

        if (effected instanceof Player)
            ((PlayerMoveController) effected.getMoveController()).setBegin(skill.getX(), skill.getY(), skill.getZ());

        World.getInstance().updatePosition(effected, skill.getX(), skill.getY(), skill.getZ(), skill.getH());
    }

    static final byte obstacle_intentions = (byte) (CollidableType.PHYSICAL.getId() | CollidableType.DOOR.getId());

    @Override
    public void calculate(Effect effect) {
        if (!super.calculate(effect, StatEnum.STAGGER_RESISTANCE, SpellStatus.STAGGER)) {
            return;
        }
        effect.addSucessEffect(this);

        Creature effector = effect.getEffector();
        Creature effected = effect.getEffected();

        float direction = 0;
        if (effect.getEffected().getMoveController().isInMove()) {
            direction = 1;
        }
        double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effector.getHeading()));
        float x1 = (float) (Math.cos(Math.PI * direction + radian) * distance);
        float y1 = (float) (Math.sin(Math.PI * direction + radian) * distance);

        Vector3f closestCollision = distance > 1 ?
                GeoEngine.getAvailablePoint(effected, x1, y1, obstacle_intentions) :
                new Vector3f(effected.getX() + x1, effected.getY() + y1, effected.getZ());

        effect.getSkill().setTargetPosition(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), effected.getHeading());
    }
}
