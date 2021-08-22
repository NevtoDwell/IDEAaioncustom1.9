/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.items.storage;

import com.google.common.collect.ImmutableList;
import javolution.util.FastMap;

import com.ne.gs.model.gameobjects.Item;

/**
 * @author KID
 */
public class ItemStorage {

    public static final int FIRST_AVAILABLE_SLOT = 65535;

    private final FastMap<Integer, Item> items;
    private int limit;

    public ItemStorage(int limit) {
        this.limit = limit;
        items = FastMap.newInstance();
    }

    public ImmutableList<Item> getItems() {
        return ImmutableList.copyOf(items.values());
    }

    public int getLimit() {
        return limit;
    }

    public boolean setLimit(int limit) {
        if (items.size() > limit) {
            return false;
        }

        this.limit = limit;
        return true;
    }

    public Item getFirstItemById(int itemId) {
        for (Item item : items.values()) {
            if (item.getItemTemplate().getTemplateId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public ImmutableList<Item> getItemsById(int itemId) {
        ImmutableList.Builder<Item> b = ImmutableList.builder();
        for (Item item : items.values()) {
            if (item.getItemTemplate().getTemplateId() == itemId) {
                b.add(item);
            }
        }
        return b.build();
    }

    public Item getItemByObjId(int itemObjId) {
        return items.get(itemObjId);
    }

    public int getSlotIdByItemId(int itemId) {
        for (Item item : items.values()) {
            if (item.getItemTemplate().getTemplateId() == itemId) {
                return item.getEquipmentSlot();
            }
        }
        return -1;
    }

    public Item getItemBySlotId(short slotId) {
        for (Item item : items.values()) {
            if (item.getEquipmentSlot() == slotId) {
                return item;
            }
        }
        return null;
    }

    public int getSlotIdByObjId(int objId) {
        Item item = getItemByObjId(objId);
        if (item != null) {
            return item.getEquipmentSlot();
        } else {
            return -1;
        }
    }

    public int getNextAvailableSlot() {
        return FIRST_AVAILABLE_SLOT;
    }

    public boolean putItem(Item item) {
        if (items.containsKey(item.getObjectId())) {
            return false;
        }

        items.put(item.getObjectId(), item);
        return true;
    }

    public Item removeItem(int objId) {
        return items.remove(objId);
    }

    public boolean isFull() {
        return items.size() >= limit;
    }

    public int getFreeSlots() {
        return limit - items.size();
    }

    public int size() {
        return items.size();
    }
    
    public boolean isFullQ() {
        return this.items.size() >= limit + 2;
    }

    public int getFreeSlotsQ() {
        return (limit + 2) - this.items.size();
    }

}
