/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * This packet is informing client that some AionObject is no longer visible.
 *
 * @author -Nemesiss-
 */
public class SM_DELETE extends AionServerPacket {

    /**
     * Object that is no longer visible.
     */
    private final int objectId;
    private final int time;

    /**
     * Constructor.
     *
     * @param object
     */

    public SM_DELETE(AionObject object, int time) {
        objectId = object.getObjectId();
        this.time = time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        int action = 0;
        if (action != 1) {
            writeD(objectId);
            writeC(time); // removal animation speed
        }
    }
}
