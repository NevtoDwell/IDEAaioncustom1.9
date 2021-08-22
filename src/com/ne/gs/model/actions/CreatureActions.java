/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.actions;

import com.ne.gs.model.gameobjects.Creature;

public final class CreatureActions {

    public static String getName(Creature creature) {
        return creature.getName();
    }

    public static boolean isAlreadyDead(Creature creature) {
        return creature.getLifeStats().isAlreadyDead();
    }

    public static void delete(Creature creature) {
        if (creature != null) {
            creature.getController().onDelete();
        }
    }
}
