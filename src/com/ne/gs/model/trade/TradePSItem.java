/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.trade;

/**
 * @author Simple
 */
public class TradePSItem extends TradeItem {

    private int itemObjId;
    private long price;

    /**
     * @param itemId
     * @param count
     */
    public TradePSItem(int itemObjId, int itemId, long count, long price) {
        super(itemId, count);
        setPrice(price);
        setItemObjId(itemObjId);
    }

    /**
     * @param price
     *     the price to set
     */
    public void setPrice(long price) {
        this.price = price;
    }

    /**
     * @return the price
     */
    public long getPrice() {
        return price;
    }

    /**
     * @param itemObjId
     *     the itemObjId to set
     */
    public void setItemObjId(int itemObjId) {
        this.itemObjId = itemObjId;
    }

    /**
     * @return the itemObjId
     */
    public int getItemObjId() {
        return itemObjId;
    }

}
