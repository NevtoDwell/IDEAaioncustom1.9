/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

public class ArtifactAssault extends Assault<ArtifactSiege> {

    public ArtifactAssault(ArtifactSiege siege) {
        super(siege);
    }

    @Override
    public void scheduleAssault(int delay) {
    }

    @Override
    public void onAssaultFinish(boolean captured) {
    }
}
