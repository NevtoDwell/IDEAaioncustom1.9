/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.flyring.FlyRing;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.utils3d.Point3D;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.utils.MathUtil;

/**
 * @author xavier, Source
 */
public class FlyRingObserver extends ActionObserver {

    private final Player player;

    private final FlyRing ring;

    private Point3D oldPosition;

    SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(1856);

    public FlyRingObserver() {
        super(ObserverType.MOVE);
        player = null;
        ring = null;
        oldPosition = null;
    }

    public FlyRingObserver(FlyRing ring, Player player) {
        super(ObserverType.MOVE);
        this.player = player;
        this.ring = ring;
        oldPosition = new Point3D(player.getX(), player.getY(), player.getZ());
    }

    @Override
    public void moved() {
        Point3D newPosition = new Point3D(player.getX(), player.getY(), player.getZ());
        boolean passedThrough = false;

        if (ring.getPlane().intersect(oldPosition, newPosition)) {
            Point3D intersectionPoint = ring.getPlane().intersection(oldPosition, newPosition);
            if (intersectionPoint != null) {
                double distance = Math.abs(ring.getPlane().getCenter().distance(intersectionPoint));

                if (distance < ring.getTemplate().getRadius() * 2) { // FIXME maybe requires tweaks
                    passedThrough = true;
                }
            } else if (MathUtil.isIn3dRange(ring, player, ring.getTemplate().getRadius())) {
                passedThrough = true;
            }
        }

        if (passedThrough) {
            if (ring.getTemplate().getMap() == 400010000 || isQuestactive() || isInstancetactive()) {
                Effect speedUp = new Effect(player, player, skillTemplate, skillTemplate.getLvl(), 0);
                speedUp.initialize();
                speedUp.addAllEffectToSucess();
                speedUp.applyEffect();
            }

            QuestEngine.getInstance().onPassFlyingRing(new QuestEnv(null, player, 0, 0), ring.getName());
        }

        oldPosition = newPosition;
    }

    private boolean isInstancetactive() {
        return ring.getPosition().getWorldMapInstance().getInstanceHandler().onPassFlyingRing(player, ring.getName());
    }

    private boolean isQuestactive() {
        int questId = player.getRace() == Race.ASMODIANS ? 2042 : 1044;
        QuestState qs = player.getQuestStateList().getQuestState(questId);

        if (qs == null) {
            return false;
        }

        return qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) >= 2 && qs.getQuestVarById(0) <= 8;
    }

}
