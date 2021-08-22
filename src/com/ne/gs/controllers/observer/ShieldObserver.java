/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.shield.Shield;
import com.ne.gs.model.utils3d.Point3D;
import com.ne.gs.services.SiegeService;
import com.ne.gs.utils.MathUtil;

/**
 * @author Wakizashi, Source
 */
public class ShieldObserver extends ActionObserver {

    private final Creature creature;
    private final Shield shield;
    private final Point3D oldPosition;

    public ShieldObserver() {
        super(ObserverType.MOVE);
        creature = null;
        shield = null;
        oldPosition = null;
    }

    public ShieldObserver(Shield shield, Creature creature) {
        super(ObserverType.MOVE);
        this.creature = creature;
        this.shield = shield;
        oldPosition = new Point3D(creature.getX(), creature.getY(), creature.getZ());
    }

    @Override
    public void moved() {
        boolean passedThrough = false;
        boolean isGM = false;

        if (SiegeService.getInstance().getFortress(shield.getId()).isUnderShield()) {
            if (!(creature.getZ() < shield.getZ() && oldPosition.getZ() < shield.getZ())) {
                if (MathUtil.isInSphere(shield, (float) oldPosition.getX(), (float) oldPosition.getY(), (float) oldPosition.getZ(), shield.getTemplate()
                    .getRadius()) != MathUtil.isIn3dRange(shield, creature, shield.getTemplate().getRadius())) {
                    passedThrough = true;
                }
            }
        }

        if (passedThrough) {
            if (creature instanceof Player) {
                ((Player) creature).sendMsg("You passed through shield.");
                isGM = ((Player) creature).isGM();
            }

            if (!isGM) {
                if (!(creature.getLifeStats().isAlreadyDead())) {
                    creature.getController().die();
                }
                if (creature instanceof Player) {
                    ((Player) creature).getFlyController().endFly(true);
                }
                creature.getObserveController().removeObserver(this);
            }
        }

        oldPosition.x = creature.getX();
        oldPosition.y = creature.getY();
        oldPosition.z = creature.getZ();
    }

}
