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

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_FORCED_MOVE;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_UPDATE;
import com.ne.gs.skillengine.model.DashStatus;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.GeoHelper;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.collision.CollisionResult;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.math.Vector3f;
import mw.utils.GeomUtil;

/**
 * @author Bio, Alex
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RandomMoveLocEffect")
public class RandomMoveLocEffect extends EffectTemplate {
    
    @XmlAttribute(name = "distance")
    private float distance;
    @XmlAttribute(name = "direction")
    private float direction;

    @Override
    public void applyEffect(Effect effect) {

        final Player effector = (Player) effect.getEffector();

        Skill skill = effect.getSkill();

        effector.getMoveController().setBegin(skill.getX(), skill.getY(), skill.getZ());
        World.getInstance().updatePosition(effector, skill.getX(), skill.getY(), skill.getZ(), skill.getH());

    }

    @Override
    public void calculate(Effect effect) {
        effect.addSucessEffect(this);
        effect.setDashStatus(DashStatus.RANDOMMOVELOC);

        final Player effector = (Player) effect.getEffector();
        Vector3f closestCollision = closestCollision(effector, direction);
        float direct = direction == 0 ? 1 : 0;
        int m = (int) (closestCollision.getZ() - effector.getZ());
        //fix 1 bag
        if (m >= 3) {
            closestCollision = new Vector3f(
                    effector.getMoveController().beginX(),
                    effector.getMoveController().beginY(),
                    effector.getMoveController().beginZ());
        }
        effect.getSkill().setTargetPosition(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), effector.getHeading());
    }

    Vector3f closestCollision(Player effector, float direct) {
        // Move Effector backwards direction=1 or frontwards direction=0
        double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effector.getHeading()));
        float x1 = (float) (Math.cos(Math.PI * direct + radian) * distance);
        float y1 = (float) (Math.sin(Math.PI * direct + radian) * distance);

        byte intentions = (byte) (CollidableType.PHYSICAL.getId() | CollidableType.DOOR.getId());

        Vector3f target = GeoEngine.getAvailablePoint(effector, x1, y1, intentions);

        return target;
    }
}
