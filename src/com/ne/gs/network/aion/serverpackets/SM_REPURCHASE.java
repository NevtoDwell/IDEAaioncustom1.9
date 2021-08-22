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

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;
import com.ne.gs.services.RepurchaseService;

/**
 * @author xTz, KID
 */
public class SM_REPURCHASE extends AionServerPacket {

    private final Player player;
    private final int targetObjectId;
    private final Collection<Item> items;

    public SM_REPURCHASE(Player player, int npcId) {
        this.player = player;
        targetObjectId = npcId;
        items = RepurchaseService.getInstance().getRepurchaseItems(player.getObjectId());
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(targetObjectId);
        writeD(1);
        writeH(items.size());

        for (Item item : items) {
            ItemTemplate itemTemplate = item.getItemTemplate();

            writeD(item.getObjectId());
            writeD(itemTemplate.getTemplateId());
            writeNameId(itemTemplate.getNameId());

            ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
            itemInfoBlob.writeMe(getBuf());

            writeQ(item.getRepurchasePrice());
        }
    }
}
