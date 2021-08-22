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

/**
 * @author kosyachok
 */
public class SM_DELETE_WAREHOUSE_ITEM extends AionServerPacket {

    private final int warehouseType;
    private final int itemObjId;
    private final ItemDeleteType deleteType;

    public SM_DELETE_WAREHOUSE_ITEM(int warehouseType, int itemObjId, ItemDeleteType deleteType) {
        this.warehouseType = warehouseType;
        this.itemObjId = itemObjId;
        this.deleteType = deleteType;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(warehouseType);
        writeD(itemObjId);
        writeC(deleteType.getMask());
    }

}
