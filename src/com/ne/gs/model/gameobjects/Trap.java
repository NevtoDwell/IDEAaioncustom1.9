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
import com.ne.gs.model.stats.container.NpcLifeStats;
import com.ne.gs.model.stats.container.TrapGameStats;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;

/**
 * @author ATracer
 */
public class Trap extends SummonedObject<Creature> {

    /**
     * @param objId
     * @param controller
     * @param spawnTemplate
     * @param objectTemplate
     */
    public Trap(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate) {
        super(objId, controller, spawnTemplate, objectTemplate, objectTemplate.getLevel());
    }

    @Override
    protected void setupStatContainers(byte level) {
        setGameStats(new TrapGameStats(this));
        setLifeStats(new NpcLifeStats(this));
    }

    @Override
    public byte getLevel() {
        return getCreator() == null ? 1 : getCreator().getLevel();
    }

    @Override
    public boolean isEnemy(Creature creature) {
        return getCreator().isEnemy(creature);
    }

    @Override
    public boolean isEnemyFrom(Player player) {
        return getCreator() != null ? getCreator().isEnemyFrom(player) : false;
    }

    /**
     * @return NpcObjectType.TRAP
     */
    @Override
    public NpcObjectType getNpcObjectType() {
        return NpcObjectType.TRAP;
    }

    @Override
    public String getMasterName() {
        return StringUtils.EMPTY;
    }
}
