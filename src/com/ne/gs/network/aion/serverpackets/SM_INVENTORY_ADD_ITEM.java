/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.List;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.ItemStorage;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;

/**
 * @author ATracer
 */
public class SM_INVENTORY_ADD_ITEM extends AionServerPacket {

    private final List<Item> items;
    private final int size;
    private final Player player;

    public SM_INVENTORY_ADD_ITEM(List<Item> items, Player player) {
        this.player = player;
        this.items = items;
        size = items.size();
    }

    @Override
    protected void writeImpl(AionConnection con) {
        // TODO! why its not use ItemAddType!?
        // 0x1C after buy, 0x35 after quest, 0x40 questionnaire;
        int mask = (size == 1 && items.get(0).getEquipmentSlot() != ItemStorage.FIRST_AVAILABLE_SLOT) ? 0x07 : 0x19;
        writeH(mask); //
        writeH(size); // number of entries
        for (Item item : items) {
            writeItemInfo(item);
        }
    }

    private void writeItemInfo(Item item) {
        ItemTemplate itemTemplate = item.getItemTemplate();

        writeD(item.getObjectId());
        writeD(itemTemplate.getTemplateId());
        writeNameId(itemTemplate.getNameId());

        ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
        itemInfoBlob.writeMe(getBuf());

        writeH(item.isEquipped() ? 255 : item.getEquipmentSlot()); // FF FF equipment
        writeC(0x00);// isEquiped?
    }
}
