/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.item.ItemSplitService;

/**
 * @author kosyak
 */
public class CM_SPLIT_ITEM extends AionClientPacket {

    int sourceItemObjId;
    byte sourceStorageType;
    long itemAmount;
    int destinationItemObjId;
    byte destinationStorageType;
    short slotNum;

    @Override
    protected void readImpl() {
        sourceItemObjId = readD();
        itemAmount = readD();

        readB(4); // Nothing

        sourceStorageType = readSC();
        destinationItemObjId = readD();
        destinationStorageType = readSC();
        slotNum = readSH();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        ItemSplitService.splitItem(player, sourceItemObjId, destinationItemObjId, itemAmount, slotNum, sourceStorageType, destinationStorageType);
    }
}
