/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.siege;

import com.ne.gs.controllers.NpcController;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.siegespawns.SiegeSpawnTemplate;

/**
 * @author ViAl
 */
public class SiegeNpc extends Npc {

    private final int siegeId;
    private final SiegeRace siegeRace;

    /**
     * @param objId
     * @param controller
     * @param spawnTemplate
     * @param objectTemplate
     *     SiegeNpc constructor
     */
    public SiegeNpc(int objId, NpcController controller, SiegeSpawnTemplate spawnTemplate, NpcTemplate objectTemplate) {
        super(objId, controller, spawnTemplate, objectTemplate);
        siegeId = spawnTemplate.getSiegeId();
        siegeRace = spawnTemplate.getSiegeRace();
    }

    public SiegeRace getSiegeRace() {
        return siegeRace;
    }

    public int getSiegeId() {
        return siegeId;
    }

    @Override
    public SiegeSpawnTemplate getSpawn() {
        return (SiegeSpawnTemplate) super.getSpawn();
    }

    @Override
    public boolean isAggressiveTo(Creature creature) {
        if ((creature instanceof SiegeNpc) && getSiegeRace() != ((SiegeNpc) creature).getSiegeRace()) {
            return true;
        }

        return super.isAggressiveTo(creature);
    }
}
