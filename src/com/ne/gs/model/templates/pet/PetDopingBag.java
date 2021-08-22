/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.pet;

import java.util.Arrays;

public class PetDopingBag {

    private int[] itemBag = null;
    private boolean isDirty = false;

    public void setFoodItem(int itemId) {
        setItem(itemId, 0);
    }

    public int getFoodItem() {
        if (itemBag == null || itemBag.length < 1) {
            return 0;
        }
        return itemBag[0];
    }

    public void setDrinkItem(int itemId) {
        setItem(itemId, 1);
    }

    public int getDrinkItem() {
        if (itemBag == null || itemBag.length < 2) {
            return 0;
        }
        return itemBag[1];
    }

    public void setItem(int itemId, int slot) {
        if (itemBag == null) {
            itemBag = new int[slot + 1];
            isDirty = true;
        } else if (slot > itemBag.length - 1) {
            itemBag = Arrays.copyOf(itemBag, slot + 1);
            isDirty = true;
        }
        if (itemBag[slot] != itemId) {
            itemBag[slot] = itemId;
            isDirty = true;
        }
    }

    public int[] getScrollsUsed() {
        if ((itemBag == null) || (itemBag.length < 3)) {
            return new int[0];
        }
        return Arrays.copyOfRange(itemBag, 2, itemBag.length);
    }

    public boolean isDirty() {
        return isDirty;
    }
}
