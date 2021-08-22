/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.items;

/**
 * @author ATracer
 */
public enum ItemId {
    KINAH(182400001);

    private final int itemId;

    private ItemId(int itemId) {
        this.itemId = itemId;
    }

    public int value() {
        return itemId;
    }
}
