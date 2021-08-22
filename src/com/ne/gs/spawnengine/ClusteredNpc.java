/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.spawnengine;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.templates.walker.WalkerTemplate;

/**
 * Stores for the spawn needed information, used for forming walker groups and spawning NPCs
 *
 * @author vlog
 * @modified Rolandas
 */
public class ClusteredNpc {

    private Npc npc;
    private final int instance;
    private final WalkerTemplate walkTemplate;
    private float x;
    private float y;
    private final int walkerIdx;

    public ClusteredNpc(Npc npc, int instance, WalkerTemplate walkTemplate) {
        this.npc = npc;
        this.instance = instance;
        this.walkTemplate = walkTemplate;
        x = npc.getSpawn().getX();
        y = npc.getSpawn().getY();
        walkerIdx = npc.getSpawn().getWalkerIndex();
    }

    public Npc getNpc() {
        return npc;
    }

    public int getInstance() {
        return instance;
    }

    public void spawn(float z) {
        SpawnEngine.bringIntoWorld(npc, npc.getSpawn().getWorldId(), instance, x, y, z, npc.getSpawn().getHeading());
    }

    public void setNpc(Npc npc) {
        npc.setWalkerGroupShift(this.npc.getWalkerGroupShift());
        this.npc = npc;
        x = npc.getSpawn().getX();
        y = npc.getSpawn().getY();
    }

    public boolean hasSamePosition(ClusteredNpc other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return x == other.x && y == other.y;
    }

    public int getPositionHash() {
        int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        return result;
    }

    /**
     * @return the x
     */
    public float getX() {
        return x;
    }

    public float getXDelta() {
        return walkTemplate.getRouteStep(1).getX() - x;
    }

    /**
     * @param x
     *     the x to set
     */
    public void setX(float x) {
        this.x = x;
        getNpc().getSpawn().setX(x);
    }

    /**
     * @return the y
     */
    public float getY() {
        return y;
    }

    public float getYDelta() {
        return walkTemplate.getRouteStep(1).getY() - y;
    }

    /**
     * @param y
     *     the y to set
     */
    public void setY(float y) {
        this.y = y;
        getNpc().getSpawn().setY(y);
    }

    /**
     * @return the walkTemplate
     */
    public WalkerTemplate getWalkTemplate() {
        return walkTemplate;
    }

    public int getWalkerIndex() {
        return walkerIdx;
    }

}
