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
 */
public class SM_GATHER_STATUS extends AionServerPacket {

    private final int status;
    private final int playerobjid;
    private final int gatherableobjid;

    public SM_GATHER_STATUS(int playerobjid, int gatherableobjid, int status) {
        this.playerobjid = playerobjid;
        this.gatherableobjid = gatherableobjid;
        this.status = status;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected void writeImpl(AionConnection con) {

        writeD(playerobjid);
        writeD(gatherableobjid);
        writeH(0); // unk
        writeC(status);

    }
}
