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
import com.ne.gs.services.BrokerService;

/**
 * @author kosyak
 */
public class CM_REGISTER_BROKER_ITEM extends AionClientPacket {

    @SuppressWarnings("unused")
    private int brokerId;
    private int itemUniqueId;
    private long price;
    private int itemCount;

    @Override
    protected void readImpl() {
        brokerId = readD();
        itemUniqueId = readD();
        price = readQ();
        itemCount = readH();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (player.isTrading() || price < 1 || itemCount < 1) {
            return;
        }

        BrokerService.getInstance().registerItem(player, itemUniqueId, price, itemCount);
    }
}
