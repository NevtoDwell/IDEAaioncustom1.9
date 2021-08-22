/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author alexa026
 */
public class SM_LOOKATOBJECT extends AionServerPacket {

    private final VisibleObject visibleObject;
    private final int targetObjectId;
    private final int heading;

    public SM_LOOKATOBJECT(VisibleObject visibleObject) {
        this.visibleObject = visibleObject;
        if (visibleObject.getTarget() != null) {
            targetObjectId = visibleObject.getTarget().getObjectId();
            heading = Math.abs(128 - visibleObject.getTarget().getHeading());
        } else {
            targetObjectId = 0;
            heading = visibleObject.getHeading();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(visibleObject.getObjectId());
        writeD(targetObjectId);
        writeC(heading);
    }
}
