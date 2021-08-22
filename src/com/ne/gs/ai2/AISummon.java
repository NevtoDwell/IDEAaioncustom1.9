/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2;

import com.ne.gs.controllers.SummonController;
import com.ne.gs.controllers.movement.SummonMoveController;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;

@AIName("summon")
public class AISummon extends AITemplate {

    @Override
    public Summon getOwner() {
        return (Summon) super.getOwner();
    }

    protected NpcTemplate getObjectTemplate() {
        return getOwner().getObjectTemplate();
    }

    protected SpawnTemplate getSpawnTemplate() {
        return getOwner().getSpawn();
    }

    protected Race getRace() {
        return getOwner().getRace();
    }

    protected Player getMaster() {
        return getOwner().getMaster();
    }

    protected SummonMoveController getMoveController() {
        return getOwner().getMoveController();
    }

    protected SummonController getController() {
        return getOwner().getController();
    }
}
