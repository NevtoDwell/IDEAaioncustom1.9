/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import java.util.LinkedHashMap;

import com.ne.gs.model.trade.TradePSItem;

/**
 * @author Xav Modified by Simple
 */
public class PrivateStore {

    private final Player owner;
    private LinkedHashMap<Integer, TradePSItem> items;
    private String storeMessage;

    /**
     * This method binds a player to the store and creates a list of items
     *
     * @param owner
     */
    public PrivateStore(Player owner) {
        this.owner = owner;
        items = new LinkedHashMap<>();
    }

    /**
     * This method will return the owner of the store
     *
     * @return Player
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * This method will return the items being sold
     *
     * @return LinkedHashMap<Integer, TradePSItem>
     */
    public LinkedHashMap<Integer, TradePSItem> getSoldItems() {
        return items;
    }

    /**
     * This method will add an item to the list and price
     *
     */
    public void addItemToSell(int itemObjId, TradePSItem tradeItem) {
        items.put(itemObjId, tradeItem);
    }

    /**
     * This method will remove an item from the list
     *
     */
    public void removeItem(int itemObjId) {
        if (items.containsKey(itemObjId)) {
            LinkedHashMap<Integer, TradePSItem> newItems = new LinkedHashMap<>();
            for (int itemObjIds : items.keySet()) {
                if (itemObjId != itemObjIds) {
                    newItems.put(itemObjIds, items.get(itemObjIds));
                }
            }
            items = newItems;
        }
    }

    /**
     */
    public TradePSItem getTradeItemByObjId(int itemObjId) {
        return items.get(itemObjId);
    }

    /**
     * @param storeMessage
     *     the storeMessage to set
     */
    public void setStoreMessage(String storeMessage) {
        this.storeMessage = storeMessage;
    }

    /**
     * @return the storeMessage
     */
    public String getStoreMessage() {
        return storeMessage;
    }
}
