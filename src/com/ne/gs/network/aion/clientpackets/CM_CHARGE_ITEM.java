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
import java.util.Collection;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.item.ItemChargeService;

/**
 * @author ATracer
 */
public class CM_CHARGE_ITEM extends AionClientPacket {

    private int targetNpcObjectId;
    private int chargeLevel;
    private Collection<Integer> itemIds;

    @Override
    protected void readImpl() {
        targetNpcObjectId = readD();
        chargeLevel = readC();
        int itemsSize = readH();
        itemIds = new ArrayList<>();
        for (int i = 0; i < itemsSize; i++) {
            itemIds.add(readD());
        }

    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (!player.isTargeting(targetNpcObjectId)) {
            return; // TODO audit?
        }

        for (int itemObjId : itemIds) {
            Item item = player.getInventory().getItemByObjId(itemObjId);
            if (item != null) {
                int itemChargeLevel = item.getChargeLevelMax();
                int possibleChargeLevel = Math.min(itemChargeLevel, chargeLevel);
                if (possibleChargeLevel > 0) {
                    if (ItemChargeService.processPayment(player, item, possibleChargeLevel)) {
                        ItemChargeService.chargeItem(player, item, possibleChargeLevel);
                    }
                }
            }
        }
    }

}
