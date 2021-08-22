/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.LinkedHashMap;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PrivateStore;
import com.ne.gs.model.trade.TradePSItem;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;

/**
 * @author Simple
 */
public class SM_PRIVATE_STORE extends AionServerPacket {

    private final Player player;
    /**
     * Private store Information *
     */
    private final PrivateStore store;

    public SM_PRIVATE_STORE(PrivateStore store, Player player) {
        this.player = player;
        this.store = store;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        if (store != null) {
            Player storePlayer = store.getOwner();
            LinkedHashMap<Integer, TradePSItem> soldItems = store.getSoldItems();

            writeD(storePlayer.getObjectId());
            writeH(soldItems.size());
            for (Integer itemObjId : soldItems.keySet()) {
                Item item = storePlayer.getInventory().getItemByObjId(itemObjId);
                TradePSItem tradeItem = store.getTradeItemByObjId(itemObjId);
                long price = tradeItem.getPrice();
                writeD(itemObjId);
                writeD(item.getItemTemplate().getTemplateId());
                writeH((int) tradeItem.getCount());
                writeD((int) price);

                ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
                itemInfoBlob.writeMe(getBuf());
            }
        }
    }
}
