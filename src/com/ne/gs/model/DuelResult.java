/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

/**
 * @author xavier
 */
public enum DuelResult {
    DUEL_WON(1300098, (byte) 2),
    DUEL_LOST(1300099, (byte) 0),
    DUEL_DRAW(1300100, (byte) 1);

    private final int msgId;
    private final byte resultId;

    private DuelResult(int msgId, byte resultId) {
        this.msgId = msgId;
        this.resultId = resultId;
    }

    public int getMsgId() {
        return msgId;
    }

    public byte getResultId() {
        return resultId;
    }
}
