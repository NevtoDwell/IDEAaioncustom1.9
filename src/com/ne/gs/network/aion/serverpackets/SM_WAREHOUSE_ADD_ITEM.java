/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;
import com.ne.gs.services.item.ItemPacketService.ItemAddType;

/**
 * @author kosyachok
 * @author -Nemesiss-
 */
public class SM_WAREHOUSE_ADD_ITEM extends AionServerPacket {

    private final int warehouseType;
    private final List<Item> items;
    private final Player player;

    public SM_WAREHOUSE_ADD_ITEM(Item item, int warehouseType, Player player) {
        this.player = player;
        this.warehouseType = warehouseType;
        items = Collections.singletonList(item);
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(warehouseType);
        writeH(ItemAddType.PUT.getMask());
        writeH(items.size());

        for (Item item : items) {
            writeItemInfo(item);
        }
    }

    private void writeItemInfo(Item item) {
        ItemTemplate itemTemplate = item.getItemTemplate();

        writeD(item.getObjectId());
        writeD(itemTemplate.getTemplateId());
        writeC(0); // some item info (4 - weapon, 7 - armor, 8 - rings, 17 - bottles)
        writeNameId(itemTemplate.getNameId());

        ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
        itemInfoBlob.writeMe(getBuf());

        writeH(item.getEquipmentSlot());
    }
}
