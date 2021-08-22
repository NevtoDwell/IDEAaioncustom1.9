/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;

/**
 * @author kosyachok
 * @author -Nemesiss-
 */
public class SM_WAREHOUSE_UPDATE_ITEM extends AionServerPacket {

    private final Player player;
    private final Item item;
    private final int warehouseType;
    private final ItemUpdateType updateType;

    public SM_WAREHOUSE_UPDATE_ITEM(Player player, Item item, int warehouseType, ItemUpdateType updateType) {
        this.player = player;
        this.item = item;
        this.warehouseType = warehouseType;
        this.updateType = updateType;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        ItemTemplate itemTemplate = item.getItemTemplate();

        writeD(item.getObjectId());
        writeC(warehouseType);
        writeNameId(itemTemplate.getNameId());

        ItemInfoBlob itemInfoBlob = new ItemInfoBlob(player, item);
        itemInfoBlob.addBlobEntry(ItemBlobType.GENERAL_INFO);
        itemInfoBlob.writeMe(getBuf());

        if (updateType.isSendable()) {
            writeH(updateType.getMask());
        }
    }
}
