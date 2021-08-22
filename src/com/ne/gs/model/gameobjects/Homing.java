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
import com.ne.gs.model.templates.item.ItemAttackType;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;

/**
 * @author ATracer
 */
public class Homing extends SummonedObject<Creature> {

    /**
     * Number of performed attacks
     */
    private int attackCount;

    /**
     * @param objId
     * @param controller
     * @param spawnTemplate
     * @param objectTemplate
     * @param level
     */
    public Homing(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate, byte level) {
        super(objId, controller, spawnTemplate, objectTemplate, level);
    }

    /**
     * @param attackCount
     *     the attackCount to set
     */
    public void setAttackCount(int attackCount) {
        this.attackCount = attackCount;
    }

    /**
     * @return the attackCount
     */
    public int getAttackCount() {
        return attackCount;
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
     * @return NpcObjectType.HOMING
     */
    @Override
    public NpcObjectType getNpcObjectType() {
        return NpcObjectType.HOMING;
    }

    @Override
    public String getMasterName() {
        return StringUtils.EMPTY;
    }

    @Override
    public ItemAttackType getAttackType() {
        if (getName().contains("fire")) {
            return ItemAttackType.MAGICAL_FIRE;
        } else if (getName().contains("stone")) {
            return ItemAttackType.MAGICAL_EARTH;
        } else if (getName().contains("water")) {
            return ItemAttackType.MAGICAL_WATER;
        } else if (getName().contains("wind") || getName().contains("cyclone")) {
            return ItemAttackType.MAGICAL_WIND;
        }
        return ItemAttackType.PHYSICAL;
    }

}
