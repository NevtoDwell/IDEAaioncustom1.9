/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.trade.RepurchaseList;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * @author xTz
 *         hex1r0:
 *         * added synchronization to prevent exploits
 *         * FIXME completely rework this fu**** service
 */
public class RepurchaseService {

    private final Multimap<Integer, Item> repurchaseItems;

    public RepurchaseService() {
        repurchaseItems = ArrayListMultimap.create();
    }

    /**
     * Save items for repurchase for this player
     */
    public synchronized void addRepurchaseItems(Player player, List<Item> items) {
        repurchaseItems.putAll(player.getObjectId(), items);
    }

    /**
     * Delete all repurchase items for this player
     */
    public synchronized void removeRepurchaseItems(Player player) {
        repurchaseItems.removeAll(player.getObjectId());
    }

    public synchronized Collection<Item> getRepurchaseItems(int playerObjectId) {
        Collection<Item> items = repurchaseItems.get(playerObjectId);
        return items != null ? items : Collections.<Item>emptyList();
    }

    public Item getRepurchaseItem(Player player, int itemObjectId) {
        Collection<Item> items = getRepurchaseItems(player.getObjectId());
        for (Item item : items) {
            if (item.getObjectId() == itemObjectId) {
                return item;
            }
        }
        return null;
    }

    public synchronized void repurchaseFromShop(Player player, RepurchaseList repurchaseList) {
        Storage inventory = player.getInventory();
        for (Item repurchaseItem : repurchaseList.getRepurchaseItems()) {
            if (!inventory.tryDecreaseKinah(repurchaseItem.getRepurchasePrice())) {
                AuditLogger.info(player, "Player try repurchase item: " + repurchaseItem.getItemId() + " count: " + repurchaseItem.getItemCount()
                    + " whithout kinah");
                return;
            }

            if (!removeRepurchaseItem(player, repurchaseItem)) {
                AuditLogger.info(player,
                    "Player might be abusing CM_BUY_ITEM try dupe item: " + repurchaseItem.getItemId() + " count: " + repurchaseItem.getItemCount());
                return;
            }

            ItemService.addItem(player, repurchaseItem);
        }
    }

    private boolean removeRepurchaseItem(Player player, Item item) {
        return repurchaseItems.get(player.getObjectId()).remove(item);
    }

    public static RepurchaseService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {

        protected static final RepurchaseService INSTANCE = new RepurchaseService();
    }

}
