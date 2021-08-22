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
 * In this packet Server is sending response for CM_RESTORE_CHARACTER.
 *
 * @author -Nemesiss-
 */
public class SM_RESTORE_CHARACTER extends AionServerPacket {

    /**
     * Character object id.
     */
    private final int chaOid;
    /**
     * True if player was restored.
     */
    private final boolean success;

    /**
     * Constructs new <tt>SM_RESTORE_CHARACTER </tt> packet
     */
    public SM_RESTORE_CHARACTER(int chaOid, boolean success) {
        this.chaOid = chaOid;
        this.success = success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(success ? 0x00 : 0x10);// unk
        writeD(chaOid);
    }
}
