/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team.legion;

/**
 * @author Simple
 */
public enum LegionHistoryType {
    CREATE(0),
    // No parameters
    JOIN(1),
    // Parameter: name
    KICK(2),
    // Parameter: name
    LEVEL_UP(3),
    // Parameter: legion level
    APPOINTED(4),
    // Parameter: legion level
    EMBLEM_REGISTER(5),
    // No parameters
    EMBLEM_MODIFIED(6),
    // No parameters
    ITEM_DEPOSIT(15),
    ITEM_WITHDRAW(16),
    KINAH_DEPOSIT(17),
    KINAH_WITHDRAW(18);

    private final byte historyType;

    private LegionHistoryType(int historyType) {
        this.historyType = (byte) historyType;
    }

    /**
     * Returns client-side id for this
     *
     * @return byte
     */
    public byte getHistoryId() {
        return historyType;
    }
}
