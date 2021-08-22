/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.trade;

import com.ne.gs.model.gameobjects.Item;

/**
 * @author ATracer
 */
public class ExchangeItem {

    private final int itemObjId;
    private long itemCount;
    private final int itemDesc;
    private Item item;

    /**
     * Used when exchange item != original item
     *
     * @param itemObjId
     * @param itemCount
     * @param item
     */
    public ExchangeItem(int itemObjId, long itemCount, Item item) {
        this.itemObjId = itemObjId;
        this.itemCount = itemCount;
        this.item = item;
        itemDesc = item.getItemTemplate().getNameId();
    }

    /**
     * @param item
     *     the item to set
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * @param countToAdd
     */
    public void addCount(long countToAdd) {
        itemCount += countToAdd;
        item.setItemCount(itemCount);
    }

    /**
     * @return the newItem
     */
    public Item getItem() {
        return item;
    }

    /**
     * @return the itemObjId
     */
    public int getItemObjId() {
        return itemObjId;
    }

    /**
     * @return the itemCount
     */
    public long getItemCount() {
        return itemCount;
    }

    /**
     * @return the itemDesc
     */
    public int getItemDesc() {
        return itemDesc;
    }
}
