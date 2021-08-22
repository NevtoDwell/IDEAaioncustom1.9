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
 * @author kosyachok
 */
public class CM_BROKER_LIST extends AionClientPacket {

    @SuppressWarnings("unused")
    private int brokerId;
    private int sortType;
    private int page;
    private int listMask;

    @Override
    protected void readImpl() {
        brokerId = readD();
        sortType = readC(); // 1 - name; 2 - level; 4 - totalPrice; 6 - price for piece
        page = readH();
        listMask = readH();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        BrokerService.getInstance().showRequestedItems(player, listMask, sortType, page, null);
    }
}
