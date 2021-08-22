/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.item;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.templates.item.ItemQuality;
import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public final class ItemInfoService {

    public static ItemQuality getQuality(int itemId) {
        return getItemTemplate(itemId).getItemQuality();
    }

    public static int getNameId(int itemId) {
        return getItemTemplate(itemId).getNameId();
    }

    public static ItemTemplate getItemTemplate(int itemId) {
        return DataManager.ITEM_DATA.getItemTemplate(itemId);
    }
}
