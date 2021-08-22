/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.limiteditems;

import javolution.util.FastList;

/**
 * @author xTz
 */
public class LimitedTradeNpc {

    private final FastList<LimitedItem> limitedItems;

    public LimitedTradeNpc(FastList<LimitedItem> limitedItems) {
        this.limitedItems = limitedItems;

    }

    public void putLimitedItems(FastList<LimitedItem> limitedItems) {
        this.limitedItems.addAll(limitedItems);
    }

    public FastList<LimitedItem> getLimitedItems() {
        return limitedItems;
    }
}
