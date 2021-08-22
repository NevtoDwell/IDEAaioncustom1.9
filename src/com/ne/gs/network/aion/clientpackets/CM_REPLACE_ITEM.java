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
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemMoveService;

/**
 * @author kosyachok
 */
public class CM_REPLACE_ITEM extends AionClientPacket {

    private byte sourceStorageType;
    private int sourceItemObjId;
    private byte replaceStorageType;
    private int replaceItemObjId;

    @Override
    protected void readImpl() {
        sourceStorageType = readSC();
        sourceItemObjId = readD();
        replaceStorageType = readSC();
        replaceItemObjId = readD();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        ItemMoveService.switchItemsInStorages(player, sourceStorageType, sourceItemObjId, replaceStorageType, replaceItemObjId);
    }

}
