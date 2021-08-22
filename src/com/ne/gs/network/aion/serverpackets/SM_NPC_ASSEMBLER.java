/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.assemblednpc.AssembledNpc;
import com.ne.gs.model.assemblednpc.AssembledNpcPart;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_NPC_ASSEMBLER extends AionServerPacket {

    private final AssembledNpc assembledNpc;
    private final int routeId;
    private final long timeOnMap;

    public SM_NPC_ASSEMBLER(AssembledNpc assembledNpc) {
        this.assembledNpc = assembledNpc;
        routeId = assembledNpc.getRouteId();
        timeOnMap = assembledNpc.getTimeOnMap();
    }

    @Override
    protected void writeImpl(AionConnection con) {

        writeD(assembledNpc.getAssembledParts().size()); // size
        for (AssembledNpcPart npc : assembledNpc.getAssembledParts()) {
            writeD(routeId); // routeId
            writeD(npc.getObject()); // objectId
            writeD(npc.getNpcId()); // npc Id
            writeD(npc.getStaticId()); // static Id
            writeQ(timeOnMap); // time
        }
    }
}
