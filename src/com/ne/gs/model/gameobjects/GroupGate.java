/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import com.ne.gs.controllers.NpcController;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;

/**
 * @author LokiReborn
 */
public class GroupGate extends SummonedObject<Creature> {

    /**
     * @param objId
     * @param controller
     * @param spawnTemplate
     * @param objectTemplate
     */
    public GroupGate(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate) {
        super(objId, controller, spawnTemplate, objectTemplate, (byte) 1);
    }

    @Override
    public boolean isEnemy(Creature creature) {
        return getCreator().isEnemy(creature);
    }

    /**
     * @return NpcObjectType.GROUPGATE
     */
    @Override
    public NpcObjectType getNpcObjectType() {
        return NpcObjectType.GROUPGATE;
    }
}
