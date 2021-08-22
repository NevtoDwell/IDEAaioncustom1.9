/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

/**
 * Represents a player who has been blocked
 *
 * @author Ben
 */
public class BlockedPlayer {

    PlayerCommonData pcd;
    String reason;

    public BlockedPlayer(PlayerCommonData pcd) {
        this(pcd, "");
    }

    public BlockedPlayer(PlayerCommonData pcd, String reason) {
        this.pcd = pcd;
        this.reason = reason;
    }

    public int getObjId() {
        return pcd.getPlayerObjId();
    }

    public String getName() {
        return pcd.getName();
    }

    public String getReason() {
        return reason;
    }

    public synchronized void setReason(String reason) {
        this.reason = reason;
    }
}
