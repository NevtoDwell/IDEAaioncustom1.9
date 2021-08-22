/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import org.apache.commons.lang3.StringUtils;

import com.ne.gs.controllers.NpcController;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;

/**
 * @author ATracer
 */
public class Servant extends SummonedObject<Creature> {

    private NpcObjectType objectType;

    /**
     * @param objId
     * @param controller
     * @param spawnTemplate
     * @param objectTemplate
     * @param level
     */
    public Servant(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate, byte level) {
        super(objId, controller, spawnTemplate, objectTemplate, level);
    }

    @Override
    public final boolean isEnemy(Creature creature) {
        return getCreator().isEnemy(creature);
    }

    @Override
    public boolean isEnemyFrom(Player player) {
        return getCreator() != null && getCreator().isEnemyFrom(player);
    }

    @Override
    public NpcObjectType getNpcObjectType() {
        return objectType;
    }

    public void setNpcObjectType(NpcObjectType objectType) {
        this.objectType = objectType;
    }

    @Override
    public String getMasterName() {
        return StringUtils.EMPTY;
    }

}
