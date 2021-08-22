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
 * @author ATracer
 * @author -Nemesiss-
 */
public class SM_INVENTORY_UPDATE_ITEM extends AionServerPacket {

    private final Player player;
    private final Item item;
    private final ItemUpdateType updateType;

    public SM_INVENTORY_UPDATE_ITEM(Player player, Item item) {
        this(player, item, ItemUpdateType.DEFAULT);
    }

    public SM_INVENTORY_UPDATE_ITEM(Player player, Item item, ItemUpdateType updateType) {
        this.player = player;
        this.item = item;
        this.updateType = updateType;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        ItemTemplate itemTemplate = item.getItemTemplate();

        writeD(item.getObjectId());
        writeNameId(itemTemplate.getNameId());

        ItemInfoBlob itemInfoBlob;
        switch (updateType) {
            case EQUIP_UNEQUIP:
                itemInfoBlob = new ItemInfoBlob(player, item);
                itemInfoBlob.addBlobEntry(ItemBlobType.EQUIPPED_SLOT);
                break;
            default:
                itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
                break;
        }
        itemInfoBlob.writeMe(getBuf());

        if (updateType.isSendable()) {
            writeH(updateType.getMask());
        }
    }
}
