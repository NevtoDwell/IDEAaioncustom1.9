/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection;

/**
 * In this packets aion client is notify quit. ie after this packet client will close connection.
 *
 * @author -Nemesiss-
 */
public class CM_DISCONNECT extends AionClientPacket {

    boolean unk;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        unk = readC() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {

        if (unk) {
            AionConnection client = getConnection();
            /**
             * We should close connection but not forced
             */
            client.closeNow();
        }
    }
}
