/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.ingameshop;

public class InGameShop {

    private byte subCategory;
    private byte category = 2;

    public byte getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(byte subCategory) {
        this.subCategory = subCategory;
    }

    public byte getCategory() {
        return category;
    }

    public void setCategory(byte category) {
        this.category = category;
    }
}
