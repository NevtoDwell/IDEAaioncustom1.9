/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.trade.TradePSItem;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.PrivateStoreService;

/**
 * @author Simple
 */
public class CM_PRIVATE_STORE extends AionClientPacket {

    /**
     * Private store information
     */
    private Player activePlayer;
    private TradePSItem[] tradePSItems;
    private int itemCount;
    private boolean cancelStore;

    @Override
    protected void readImpl() {
        /**
         * Define who wants to create a private store
         */
        activePlayer = getConnection().getActivePlayer();
        if (activePlayer == null) {
            return;
        }
        if (activePlayer.isInPrison()) {
            cancelStore = true;
            activePlayer.sendMsg("You can't open Private Shop in prison!");
            return;
        }

        /**
         * Read the amount of items that need to be put into the player's store
         */
        itemCount = readH();
        tradePSItems = new TradePSItem[itemCount];

        if (activePlayer.getMoveController().isInMove()) {
            activePlayer.sendPck(SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_MOVING_OBJECT);
            cancelStore = true;
            return;
        }

        for (int i = 0; i < itemCount; i++) {
            int itemObjId = readD();
            int itemId = readD();
            int count = readH();
            long price = readD();
            Item item = activePlayer.getInventory().getItemByObjId(itemObjId);
            if ((price < 0 || item == null || item.getItemId() != itemId || item.getItemCount() < count) && !cancelStore) {
                activePlayer.sendMsg("Invalid item.");
                cancelStore = true;
            } else if (!item.isTradeable(activePlayer)) {
                activePlayer.sendPck(new SM_SYSTEM_MESSAGE(1300344, DescId.of(item.getNameID())));
                cancelStore = true;
            }

            tradePSItems[i] = new TradePSItem(itemObjId, itemId, count, price);
        }
    }

    @Override
    protected void runImpl() {
        if (activePlayer == null) {
            return;
        }
        if (activePlayer.getLifeStats().isAlreadyDead()) {
            return;
        }
        if (!cancelStore && itemCount > 0) {
            PrivateStoreService.addItems(activePlayer, tradePSItems);
        } else {
            PrivateStoreService.closePrivateStore(activePlayer);
        }
    }
}
