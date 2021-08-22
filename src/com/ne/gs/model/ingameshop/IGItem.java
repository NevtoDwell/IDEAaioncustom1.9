/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.ingameshop;

/**
 * @author xTz
 */
public class IGItem {

    private final int objectId;
    private final int itemId;
    private final long itemCount;
    private final long itemPrice;
    private final byte category;
    private final byte subCategory;
    private final int list;
    private int salesRanking;
    private final byte itemType;
    private final byte gift;
    private final String titleDescription;
    private final String itemDescription;

    public IGItem(int objectId, int itemId, long itemCount, long itemPrice, byte category, byte subCategory,
                  int list, int salesRanking, byte itemType, byte gift, String titleDescription, String itemDescription) {
        this.objectId = objectId;
        this.itemId = itemId;
        this.itemCount = itemCount;
        this.itemPrice = itemPrice;
        this.category = category;
        this.subCategory = subCategory;
        this.list = list;
        this.salesRanking = salesRanking;
        this.itemType = itemType;
        this.gift = gift;
        this.titleDescription = titleDescription;
        this.itemDescription = itemDescription;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getItemId() {
        return itemId;
    }

    public long getItemCount() {
        return itemCount;
    }

    public long getItemPrice() {
        return itemPrice;
    }

    public byte getCategory() {
        return category;
    }

    public byte getSubCategory() {
        return subCategory;
    }

    public int getList() {
        return list;
    }

    public int getSalesRanking() {
        return salesRanking;
    }

    public byte getItemType() {
        return itemType;
    }

    public byte getGift() {
        return gift;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getTitleDescription() {
        return titleDescription;
    }

    public void increaseSales() {
        salesRanking++;
    }
}
