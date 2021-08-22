/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.BrokerService;

/**
 * @author namedrisk
 */
public class CM_BROKER_SEARCH extends AionClientPacket {

    @SuppressWarnings("unused")
    private int brokerId;
    private int sortType;
    private int page;
    private int mask;
    private int itemCount;
    private List<Integer> itemList;

    @Override
    protected void readImpl() {
        brokerId = readD();
        sortType = readC(); // 1 - name; 2 - level; 4 - totalPrice; 6 - price for piece
        page = readH();
        mask = readH();
        itemCount = readH();
        itemList = new ArrayList<>();

        for (int index = 0; index < itemCount; index++) {
            itemList.add(readD());
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        BrokerService.getInstance().showRequestedItems(player, mask, sortType, page, itemList);
    }
}
