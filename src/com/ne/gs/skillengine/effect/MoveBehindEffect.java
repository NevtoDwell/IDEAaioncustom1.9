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

import com.ne.gs.controllers.attack.AttackUtil;
import com.ne.gs.controllers.movement.PlayerMoveController;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.action.DamageType;
import com.ne.gs.skillengine.model.DashStatus;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.world.World;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.GeoHelper;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.math.Vector3f;

/**
 * @author Sarynth
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MoveBehindEffect")
public class MoveBehindEffect extends DamageEffect {

    @Override
    public void applyEffect(Effect effect) {
        super.applyEffect(effect);
    }

    @Override
    public void calculate(Effect effect) {
        if (effect.getEffected() == null) {
            return;
        }
        if (!(effect.getEffector() instanceof Player)) {
            return;
        }
        Player effector = (Player) effect.getEffector();
        Creature effected = effect.getEffected();
        double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effected.getHeading()));
        float x1 = (float) (Math.cos(Math.PI + radian) * 1.3F);
        float y1 = (float) (Math.sin(Math.PI + radian) * 1.3F);

        Vector3f targetPos = GeoEngine.getAvailablePoint(effected, x1, y1, CollidableType.PHYSICAL.getId());

        effector.getMoveController().abortMove();

        AttackUtil.removeTargetFrom(effector);

        effect.setDashStatus(DashStatus.MOVEBEHIND);

        effector.getMoveController().setBegin(targetPos.x, targetPos.y, targetPos.z);
        World.getInstance().updatePosition(effector, targetPos.x, targetPos.y, targetPos.z, effected.getHeading());

        effect.getSkill().setTargetPosition(targetPos.x, targetPos.y, targetPos.z, effected.getHeading());

        super.calculate(effect, DamageType.PHYSICAL);
    }
}
