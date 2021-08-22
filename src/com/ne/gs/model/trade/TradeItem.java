/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.trade;

import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class TradeItem {

    private final int itemId;
    private long count;
    private ItemTemplate itemTemplate;

    public TradeItem(int itemId, long count) {
        super();
        this.itemId = itemId;
        this.count = count;
    }

    /**
     * @return the itemTemplate
     */
    public ItemTemplate getItemTemplate() {
        return itemTemplate;
    }

    /**
     * @param itemTemplate
     *     the itemTemplate to set
     */
    public void setItemTemplate(ItemTemplate itemTemplate) {
        this.itemTemplate = itemTemplate;
    }

    /**
     * @return the itemId
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * @return the count
     */
    public long getCount() {
        return count;
    }

    /**
     * This method will decrease the current count
     */
    public void decreaseCount(long decreaseCount) {
        // TODO probably <= count ?
        if (decreaseCount < count) {
            count = count - decreaseCount;
        }
    }
}
