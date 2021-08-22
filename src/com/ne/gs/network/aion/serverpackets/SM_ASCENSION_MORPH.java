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
 * ascension quest's morph
 *
 * @author wylovech
 */
public class SM_ASCENSION_MORPH extends AionServerPacket {

    private final int inascension;

    public SM_ASCENSION_MORPH(int inascension) {
        this.inascension = inascension;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(inascension);// if inascension =0x01 morph.
        writeC(0x00); // new 2.0 Packet --- probably pet info?
    }
}
