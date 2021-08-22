/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.trade;

import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.RepurchaseService;

/**
 * @author xTz
 */
public class RepurchaseList {

    private final int sellerObjId;
    private final List<Item> repurchases = new ArrayList<>();

    public RepurchaseList(int sellerObjId) {
        this.sellerObjId = sellerObjId;
    }

    /**
     * @param player
     * @param itemObjectId
     * @param count
     */
    public void addRepurchaseItem(Player player, int itemObjectId, long count) {
        Item item = RepurchaseService.getInstance().getRepurchaseItem(player, itemObjectId);
        if (item != null) {
            repurchases.add(item);
        }
    }

    /**
     * @return the tradeItems
     */
    public List<Item> getRepurchaseItems() {
        return repurchases;
    }

    public int size() {
        return repurchases.size();
    }

    public final int getSellerObjId() {
        return sellerObjId;
    }
}
