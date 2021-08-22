/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.limiteditems;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author xTz
 */
public class LimitedItem {

    private int itemId;
    private int sellLimit;
    private int buyLimit;
    private int defaultSellLimit;
    private String salesTime;

    private final TIntObjectHashMap<Integer> buyCounts = new TIntObjectHashMap<>();

    public LimitedItem() {
    }

    public LimitedItem(int itemId, int sellLimit, int buyLimit, String salesTime) {
        this.itemId = itemId;
        this.sellLimit = sellLimit;
        this.buyLimit = buyLimit;
        defaultSellLimit = sellLimit;
        this.salesTime = salesTime;
    }

    /**
     * return itemId.
     */
    public int getItemId() {
        return itemId;
    }

    /**
     */
    public void setBuyCount(int playerObjectId, int count) {
        buyCounts.putIfAbsent(playerObjectId, count);
    }

    /**
     * return playerListByObject.
     */
    public TIntObjectHashMap<Integer> getBuyCount() {
        return buyCounts;
    }

    /**
     */
    public void setItem(int itemId) {
        this.itemId = itemId;
    }

    /**
     * return sellLimit.
     */
    public int getSellLimit() {
        return sellLimit;
    }

    /**
     * return buyLimit.
     */
    public int getBuyLimit() {
        return buyLimit;
    }

    public void setToDefault() {
        sellLimit = defaultSellLimit;
        buyCounts.clear();
    }

    /**
     */
    public void setSellLimit(int sellLimit) {
        this.sellLimit = sellLimit;
    }

    /**
     * return defaultSellLimit.
     */
    public int getDefaultSellLimit() {
        return defaultSellLimit;
    }

    public String getSalesTime() {
        return salesTime;
    }
}
