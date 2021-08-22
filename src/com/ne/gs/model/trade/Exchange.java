/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.trade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public class Exchange {

    private final Player activeplayer;
    private final Player targetPlayer;

    private boolean confirmed;
    private boolean locked;

    private long kinahCount;

    private final Map<Integer, ExchangeItem> items = new HashMap<>();
    private final List<Item> itemsToUpdate = FastList.newInstance();

    public Exchange(Player activeplayer, Player targetPlayer) {
        super();
        this.activeplayer = activeplayer;
        this.targetPlayer = targetPlayer;
    }

    public void confirm() {
        confirmed = true;
    }

    /**
     * @return the confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    public void lock() {
        locked = true;
    }

    /**
     * @return the locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param exchangeItem
     */
    public void addItem(int parentItemObjId, ExchangeItem exchangeItem) {
        items.put(parentItemObjId, exchangeItem);
    }

    /**
     * @param countToAdd
     */
    public void addKinah(long countToAdd) {
        kinahCount += countToAdd;
    }

    /**
     * @return the activeplayer
     */
    public Player getActiveplayer() {
        return activeplayer;
    }

    /**
     * @return the targetPlayer
     */
    public Player getTargetPlayer() {
        return targetPlayer;
    }

    /**
     * @return the kinahCount
     */
    public long getKinahCount() {
        return kinahCount;
    }

    /**
     * @return the items
     */
    public Map<Integer, ExchangeItem> getItems() {
        return items;
    }

    public boolean isExchangeListFull() {
        return items.size() > 18;
    }

    /**
     * @return the itemsToUpdate
     */
    public List<Item> getItemsToUpdate() {
        return itemsToUpdate;
    }

    /**
     * @param item
     */
    public void addItemToUpdate(Item item) {
        itemsToUpdate.add(item);
    }
}
