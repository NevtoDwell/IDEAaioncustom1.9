/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.autogroup;

import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author xTz
 */
public class LookingForParty {

    private final List<SearchInstance> searchInstances = new ArrayList<>();
    private boolean canRegister = true;
    private Player player;
    private long startEnterTime;

    public LookingForParty(Player player, byte instanceMaskId, EntryRequestType ert) {
        this.player = player;
        searchInstances.add(new SearchInstance(instanceMaskId, ert));
    }

    public List<Byte> getInstanceMaskIds() {
        List<Byte> instanceMaskIds = new ArrayList<>();
        for (SearchInstance si : searchInstances) {
            instanceMaskIds.add(si.getInstanceMaskId());
        }
        return instanceMaskIds;
    }

    public int unregisterInstance(byte instanceMaskId) {
        for (SearchInstance si : searchInstances) {
            if (si.getInstanceMaskId() == instanceMaskId) {
                searchInstances.remove(si);
                return searchInstances.size();
            }
        }
        return searchInstances.size();
    }

    public void addInstanceMaskId(byte instanceMaskId, EntryRequestType ert) {
        searchInstances.add(new SearchInstance(instanceMaskId, ert));
    }

    public List<SearchInstance> getSearchInstances() {
        return searchInstances;
    }

    public SearchInstance getSearchInstance(byte instanceMaskId) {
        for (SearchInstance si : searchInstances) {
            if (si.getInstanceMaskId() == instanceMaskId) {
                return si;
            }
        }
        return null;
    }

    public boolean isRegistredInstance(byte instanceMaskId) {
        for (SearchInstance si : searchInstances) {
            if (si.getInstanceMaskId() == instanceMaskId) {
                return true;
            }
        }
        return false;
    }

    public boolean isInvited(byte instanceMaskId) {
        return getSearchInstance(instanceMaskId).isInvited();
    }

    public boolean isInvited() {
        for (SearchInstance si : searchInstances) {
            if (si.isInvited()) {
                return true;
            }
        }
        return false;
    }

    public boolean canRegister() {
        return canRegister;
    }

    public void setRejecRegistration(boolean canRegister) {
        this.canRegister = canRegister;
    }

    public void setInvited(byte instanceMaskId, boolean isInvited) {
        getSearchInstance(instanceMaskId).setInvited(isInvited);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setStartEnterTime() {
        startEnterTime = System.currentTimeMillis();
    }

    public void clearStartEnterTime() {
        startEnterTime = 0;
    }

    public boolean isOnStartEnterTask() {
        if (System.currentTimeMillis() - startEnterTime <= (120000 * 2)) {
            return true;
        }
        return false;
    }
}
