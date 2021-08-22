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
import com.ne.gs.services.item.ItemPacketService.ItemDeleteType;

//Author Avol

public class SM_DELETE_ITEM extends AionServerPacket {

    private final int itemObjectId;
    private final ItemDeleteType deleteType;

    public SM_DELETE_ITEM(int itemObjectId) {
        this(itemObjectId, ItemDeleteType.UNKNOWN);
    }

    public SM_DELETE_ITEM(int itemObjectId, ItemDeleteType deleteType) {
        this.itemObjectId = itemObjectId;
        this.deleteType = deleteType;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(itemObjectId);
        writeC(deleteType.getMask());
    }
}
