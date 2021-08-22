/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public final class ItemFactory {

    private static final Logger log = LoggerFactory.getLogger(ItemFactory.class);

    public static Item newItem(int itemId) {
        ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
        if (itemTemplate == null) {
            log.error("Item was not populated correctly. Item template is missing for item id: " + itemId);
            return null;
        }
        return new Item(IDFactory.getInstance().nextId(), itemTemplate);
    }

    public static Item newItem(int itemId, long count) {
        Item item = newItem(itemId);
        item.setItemCount(calculateCount(item.getItemTemplate(), count));
        return item;
    }

    private static long calculateCount(ItemTemplate itemTemplate, long count) {
        long maxStackCount = itemTemplate.getMaxStackCount();
        if (count > maxStackCount && !itemTemplate.isKinah()) {
            count = maxStackCount;
        }
        return count;
    }

}
