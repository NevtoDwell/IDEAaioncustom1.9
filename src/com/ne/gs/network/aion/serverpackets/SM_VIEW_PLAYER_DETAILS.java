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
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;

/**
 * @author Avol, xTz
 */
public class SM_VIEW_PLAYER_DETAILS extends AionServerPacket {

    private final List<Item> items;
    private final int itemSize;
    private final int targetObjId;
    private final Player player;

    public SM_VIEW_PLAYER_DETAILS(List<Item> items, Player player) {
        this.player = player;
        targetObjId = player.getObjectId();
        this.items = items;
        itemSize = items.size();
    }

    @Override
    protected void writeImpl(AionConnection con) {

        writeD(targetObjId);
        writeC(11);
        writeH(itemSize);
        for (Item item : items) {
            writeItemInfo(item);
        }
    }

    private void writeItemInfo(Item item) {
        ItemTemplate template = item.getItemTemplate();

        writeD(0);
        writeD(template.getTemplateId());
        writeNameId(template.getNameId());

        ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
        itemInfoBlob.writeMe(getBuf());
    }
}
