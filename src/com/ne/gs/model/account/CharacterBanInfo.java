/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.account;

/**
 * @author nrg
 */
public class CharacterBanInfo {

    private final int playerId;
    private final long start;
    private final long end;
    private final String reason;

    public CharacterBanInfo(int playerId, long start, long duration, String reason) {
        this.playerId = playerId;
        this.start = start;
        end = duration + start;
        this.reason = (reason.equals("") ? "You are suspected to have violated the server's rules" : reason);
    }

    /**
     * @return the playerId
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public long getEnd() {
        return end;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }
}
