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

/**
 * @author synchro2
 */
public class CraftCooldownList {

    private FastMap<Integer, Long> craftCooldowns;

    CraftCooldownList(Player owner) {
    }

    public boolean isCanCraft(int delayId) {
        if (craftCooldowns == null || !craftCooldowns.containsKey(delayId)) {
            return true;
        }

        Long coolDown = craftCooldowns.get(delayId);
        if (coolDown == null) {
            return true;
        }

        if (coolDown < System.currentTimeMillis()) {
            craftCooldowns.remove(delayId);
            return true;
        }

        return false;
    }

    public long getCraftCooldown(int delayId) {
        if (craftCooldowns == null || !craftCooldowns.containsKey(delayId)) {
            return 0;
        }

        return craftCooldowns.get(delayId);
    }

    public FastMap<Integer, Long> getCraftCoolDowns() {
        return craftCooldowns;
    }

    public void setCraftCoolDowns(FastMap<Integer, Long> craftCoolDowns) {
        craftCooldowns = craftCoolDowns;
    }

    public void addCraftCooldown(int delayId, int delay) {
        if (craftCooldowns == null) {
            craftCooldowns = new FastMap<>();
        }

        long nextUseTime = System.currentTimeMillis() + (delay * 1000);
        craftCooldowns.put(delayId, nextUseTime);
    }
}
