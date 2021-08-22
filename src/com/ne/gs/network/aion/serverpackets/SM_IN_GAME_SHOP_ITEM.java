/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.ingameshop.IGItem;
import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author xTz, KID
 */
public class SM_IN_GAME_SHOP_ITEM extends AionServerPacket {

    private final IGItem item;

    public SM_IN_GAME_SHOP_ITEM(Player player, int objectItem) {
        item = InGameShopEn.getInstance().getIGItem(objectItem);
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(item.getObjectId());
        writeQ(item.getItemPrice());
        writeH(0);
        writeD(item.getItemId());
        writeQ(item.getItemCount());
        writeD(0);
        writeD(item.getGift());
        writeD(item.getItemType());
        writeD(0);
        writeC(0);
        writeH(0);
        writeS(item.getTitleDescription());
        writeS(item.getItemDescription());
    }
}
