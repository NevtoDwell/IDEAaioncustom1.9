/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.StaticDoor;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_GATHERABLE_INFO extends AionServerPacket {

    private final VisibleObject visibleObject;

    public SM_GATHERABLE_INFO(VisibleObject visibleObject) {
        super();
        this.visibleObject = visibleObject;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeF(visibleObject.getX());
        writeF(visibleObject.getY());
        writeF(visibleObject.getZ());
        writeD(visibleObject.getObjectId());
        writeD(visibleObject.getSpawn().getStaticId());
        writeD(visibleObject.getObjectTemplate().getTemplateId());
        if (visibleObject instanceof StaticDoor) {
            if (((StaticDoor) visibleObject).isOpen()) {
                writeH(0x09);
            } else {
                writeH(0x0A);
            }
        } else {
            writeH(1);
        }
        writeC(visibleObject.getSpawn().getHeading());
        writeD(visibleObject.getObjectTemplate().getNameId());
        writeH(0);
        writeH(0);
        writeH(0);
        writeC(100); // unk
    }
}
