/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import javolution.util.FastMap;

import com.ne.gs.network.aion.serverpackets.SM_INSTANCE_INFO;

/**
 * @author ATracer
 */
public class PortalCooldownList {

    private final Player owner;
    private FastMap<Integer, Long> portalCooldowns;

    /**
     * @param owner
     */
    PortalCooldownList(Player owner) {
        this.owner = owner;
    }

    /**
     * @param worldId
     *     * @return
     */
    public boolean isPortalUseDisabled(int worldId) {
        if (portalCooldowns == null || !portalCooldowns.containsKey(worldId)) {
            return false;
        }

        Long coolDown = portalCooldowns.get(worldId);
        if (coolDown == null) {
            return false;
        }

        if (coolDown < System.currentTimeMillis()) {
            portalCooldowns.remove(worldId);
            return false;
        }

        return true;
    }

    /**
     * @param worldId
     *
     * @return
     */
    public long getPortalCooldown(int worldId) {
        if (portalCooldowns == null || !portalCooldowns.containsKey(worldId)) {
            return 0;
        }

        return portalCooldowns.get(worldId);
    }

    public FastMap<Integer, Long> getPortalCoolDowns() {
        return portalCooldowns;
    }

    public void setPortalCoolDowns(FastMap<Integer, Long> portalCoolDowns) {
        portalCooldowns = portalCoolDowns;
    }

    /**
     * @param worldId
     */
    public void addPortalCooldown(int worldId, long useDelay) {
        if (portalCooldowns == null) {
            portalCooldowns = new FastMap<>();
        }

        long nextUseTime = System.currentTimeMillis() + useDelay;
        portalCooldowns.put(worldId, nextUseTime);

        if (owner.isInTeam()) {
            owner.getCurrentTeam().sendPacket(new SM_INSTANCE_INFO(owner, worldId));
        } else {
            owner.sendPck(new SM_INSTANCE_INFO(owner, worldId));
        }
    }

    /**
     * @param worldId
     */
    public void removePortalCoolDown(int worldId) {
        if (portalCooldowns != null) {
            portalCooldowns.remove(worldId);
        }
    }

    /**
     * @return
     */
    public boolean hasCooldowns() {
        return portalCooldowns != null && portalCooldowns.size() > 0;
    }

    /**
     * @return
     */
    public int size() {
        return portalCooldowns != null ? portalCooldowns.size() : 0;
    }

}
