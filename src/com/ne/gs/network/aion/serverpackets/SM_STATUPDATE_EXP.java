/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * This packet is used to update current exp / recoverable exp / max exp values.
 *
 * @author Luno
 * @updated by alexa026
 */
public class SM_STATUPDATE_EXP extends AionServerPacket {

    private final long currentExp;
    private final long recoverableExp;
    private final long maxExp;

    private long curBoostExp = 0;
    private long maxBoostExp = 0;

    /**
     * @param currentExp
     * @param recoverableExp
     * @param maxExp
     */
    public SM_STATUPDATE_EXP(long currentExp, long recoverableExp, long maxExp, long rep1, long rep2) {
        this.currentExp = currentExp;
        this.recoverableExp = recoverableExp;
        this.maxExp = maxExp;
        curBoostExp = rep1;
        maxBoostExp = rep2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeQ(currentExp);
        writeQ(recoverableExp);
        writeQ(maxExp);
        writeQ(curBoostExp);
        writeQ(maxBoostExp);
    }

}