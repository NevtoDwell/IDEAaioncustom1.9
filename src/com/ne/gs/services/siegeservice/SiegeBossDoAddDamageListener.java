/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import com.ne.gs.controllers.attack.AggroList;
import com.ne.gs.controllers.attack.AggroList.AddDamageValueCallback;
import com.ne.gs.model.gameobjects.Creature;

public class SiegeBossDoAddDamageListener extends AddDamageValueCallback {
    private final Siege siege;

    public SiegeBossDoAddDamageListener(Siege siege) {
        this.siege = siege;
    }

    @Override
    public void onDamageAdded(AggroList aggroList, Creature creature, int hate) {
        siege.addBossDamage(creature, hate);
    }
}
