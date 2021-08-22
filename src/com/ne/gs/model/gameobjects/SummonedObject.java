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
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.NpcLifeStats;
import com.ne.gs.model.stats.container.SummonedObjectGameStats;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;

/**
 * @author ATracer
 */
public class SummonedObject<T extends VisibleObject> extends Npc {

    private final byte level;

    /**
     * Creator of this SummonedObject
     */
    private T creator;

    /**
     * @param objId
     * @param controller
     * @param spawnTemplate
     * @param objectTemplate
     * @param level
     */
    public SummonedObject(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate, byte level) {
        super(objId, controller, spawnTemplate, objectTemplate, level);
        this.level = level;
    }

    @Override
    protected void setupStatContainers(byte level) {
        setGameStats(new SummonedObjectGameStats(this));
        setLifeStats(new NpcLifeStats(this));
    }

    @Override
    public byte getLevel() {
        return this.level;
    }

    @Override
    public T getCreator() {
        return creator;
    }

    public void setCreator(T creator) {
        if (creator instanceof Player) {
            ((Player) creator).setSummonedObj(this);
        }
        this.creator = creator;
    }

    @Override
    public String getMasterName() {
        return creator != null ? creator.getName() : StringUtils.EMPTY;
    }

    @Override
    public int getCreatorId() {
        return creator != null ? creator.getObjectId() : 0;
    }

    @Override
    public Creature getActingCreature() {
        if (creator instanceof Creature) {
            return (Creature) getCreator();
        }
        return this;
    }

    @Override
    public Creature getMaster() {
        if (creator instanceof Creature) {
            return (Creature) getCreator();
        }
        return this;
    }

    @Override
    public Race getRace() {
        if (creator instanceof Creature) {
            return ((Creature) creator).getRace();
        }
        return super.getRace();
    }
}
