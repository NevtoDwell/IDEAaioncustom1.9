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
import com.ne.gs.utils.MathUtil;

public abstract class GaleCycloneObserver extends ActionObserver {

    private final Player player;
    private final Creature creature;
    private double oldRange;

    public GaleCycloneObserver(Player player, Creature creature) {
        super(ObserverType.MOVE);
        this.player = player;
        this.creature = creature;
        oldRange = MathUtil.getDistance(player, creature);
    }

    @Override
    public void moved() {
        double newRange = MathUtil.getDistance(player, creature);
        if ((creature == null) || (creature.getLifeStats().isAlreadyDead())) {
            if (player != null) {
                player.getObserveController().removeObserver(this);
            }
            return;
        }
        if ((oldRange > 12.0D) && (newRange <= 12.0D)) {
            onMove();
        }
        oldRange = newRange;
    }

    public abstract void onMove();
}
