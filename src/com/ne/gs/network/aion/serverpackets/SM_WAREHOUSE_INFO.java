/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;
import java.util.Collections;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;

/**
 * @author kosyachok
 */
public class SM_WAREHOUSE_INFO extends AionServerPacket {

    private final int warehouseType;
    private final Collection<Item> itemList;
    private final boolean firstPacket;
    private final int expandLvl;
    private final Player player;

    public SM_WAREHOUSE_INFO(Collection<Item> items, int warehouseType, int expandLvl, boolean firstPacket, Player player) {
        this.warehouseType = warehouseType;
        this.expandLvl = expandLvl;
        this.firstPacket = firstPacket;
        if (items == null) {
            itemList = Collections.emptyList();
        } else {
            itemList = items;
        }
        this.player = player;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(warehouseType);
        writeC(firstPacket ? 1 : 0);
        writeC(expandLvl); // warehouse expand (0 - 9)
        writeH(0);
        writeH(itemList.size());
        for (Item item : itemList) {
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

        writeH(item.isEquipped() ? 255 : item.getEquipmentSlot()); // FF FF equipment
    }
}
