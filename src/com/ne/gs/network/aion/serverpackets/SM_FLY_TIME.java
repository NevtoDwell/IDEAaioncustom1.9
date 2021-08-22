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
 * @author Nemiroff
 */
public class SM_FLY_TIME extends AionServerPacket {

    private final int currentFp;
    private final int maxFp;

    public SM_FLY_TIME(int currentFp, int maxFp) {
        this.currentFp = currentFp;
        this.maxFp = maxFp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(currentFp); // current fly time
        writeD(maxFp); // max flytime
    }
}
