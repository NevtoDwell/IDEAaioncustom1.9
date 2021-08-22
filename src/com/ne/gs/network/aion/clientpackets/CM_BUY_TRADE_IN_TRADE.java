/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.TradeService;

/**
 * @author MrPoke
 */
public class CM_BUY_TRADE_IN_TRADE extends AionClientPacket {

    private int sellerObjId;
    private int itemId;
    private int count;

    @Override
    protected void readImpl() {
        sellerObjId = readD();
        itemId = readD();
        count = readD();
        // Have more data, need ?:)
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (count < 1) {
            return;
        }
        TradeService.performBuyFromTradeInTrade(player, sellerObjId, itemId, count);
    }

}
