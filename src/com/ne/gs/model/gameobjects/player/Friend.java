/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.gameobjects.player.FriendList.Status;
import com.ne.gs.world.WorldPosition;

/**
 * @author Ben
 */
public class Friend {

    private static final Logger log = LoggerFactory.getLogger(Friend.class);
    private final PlayerCommonData pcd;

    public Friend(PlayerCommonData pcd) {
        this.pcd = pcd;
    }

    /**
     * Returns the status of this player
     *
     * @return Friend's status
     */
    public Status getStatus() {
        // second check is temporary
        if (pcd.getPlayer() == null || !pcd.isOnline()) {
            return FriendList.Status.OFFLINE;
        }
        return pcd.getPlayer().getFriendList().getStatus();
    }

    /**
     * Returns this friend's name
     *
     * @return Friend's name
     */
    public String getName() {
        return pcd.getName();
    }

    public int getLevel() {
        return pcd.getLevel();
    }

    public String getNote() {
        return pcd.getNote();
    }

    public PlayerClass getPlayerClass() {
        return pcd.getPlayerClass();
    }

    public int getMapId() {
        WorldPosition position = pcd.getPosition();
        if (position == null) {
            // doubt its possible, but need check warnings
            log.warn("Null friend position: {}", pcd.getPlayerObjId());
            return 0;
        }
        return position.getMapId();
    }

    /**
     * Gets the last time this player was online as a unix timestamp<br />
     * Returns 0 if the player is online now
     *
     * @return Unix timestamp the player was last online
     */
    public int getLastOnlineTime() {
        if (pcd.getLastOnline() == null || isOnline()) {
            return 0;
        }

        return (int) (pcd.getLastOnline().getTime() / 1000); // Convert to int, unix time format (ms -> seconds)
    }

    public int getOid() {
        return pcd.getPlayerObjId();
    }

    public Player getPlayer() {
        return pcd.getPlayer();
    }

    public boolean isOnline() {
        return pcd.isOnline();
    }
}
