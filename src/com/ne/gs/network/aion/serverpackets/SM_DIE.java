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
 * @author orz
 * @author Sarynth thx Rhys2002 for packets. :)
 */
public class SM_DIE extends AionServerPacket {

    private final boolean hasRebirth;
    private final boolean hasItem;
    private final int remainingKiskTime;
    private int type = 0;
    private boolean invasion;

    public SM_DIE(boolean hasRebirth, boolean hasItem, int remainingKiskTime, int type) {
        this.hasRebirth = hasRebirth;
        this.hasItem = hasItem;
        this.remainingKiskTime = remainingKiskTime;
        this.type = type;
    }
    
    public SM_DIE(boolean hasRebirth, boolean hasItem, int remainingKiskTime, int type, boolean invasion) {
        this.hasRebirth = hasRebirth;
        this.hasItem = hasItem;
        this.remainingKiskTime = remainingKiskTime;
        this.type = type;
        this.invasion = invasion;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC((hasRebirth ? 1 : 0)); // skillRevive
        writeC((hasItem ? 1 : 0)); // itemRevive
        writeD(remainingKiskTime);
        writeC(type);
        writeC(invasion ? 0x80 : 0x00);
    }
}
