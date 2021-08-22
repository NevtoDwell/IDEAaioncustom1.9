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

/**
 * @author Avol
 * @author ATracer
 */
public class SM_EXCHANGE_ADD_ITEM extends AionServerPacket {

    private final Player player;
    private final int action;
    private final Item item;

    public SM_EXCHANGE_ADD_ITEM(int action, Item item, Player player) {
        this.player = player;
        this.action = action;
        this.item = item;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        ItemTemplate itemTemplate = item.getItemTemplate();

        writeC(action); // 0 -self 1-other

        writeD(itemTemplate.getTemplateId());
        writeD(item.getObjectId());
        writeNameId(itemTemplate.getNameId());

        ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
        itemInfoBlob.writeMe(getBuf());
    }
}
