/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.items;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum is defining inventory slots, to which items can be equipped.
 *
 * @author Luno
 */
public enum ItemSlot {
    MAIN_HAND(1),
    SUB_HAND(1 << 1),
    HELMET(1 << 2),
    TORSO(1 << 3),
    GLOVES(1 << 4),
    BOOTS(1 << 5),
    EARRINGS_LEFT(1 << 6),
    EARRINGS_RIGHT(1 << 7),
    RING_LEFT(1 << 8),
    RING_RIGHT(1 << 9),
    NECKLACE(1 << 10),
    SHOULDER(1 << 11),
    PANTS(1 << 12),
    POWER_SHARD_RIGHT(1 << 13),
    POWER_SHARD_LEFT(1 << 14),
    WINGS(1 << 15),
    // non-NPC equips (slot > Short.MAX)
    WAIST(1 << 16),
    MAIN_OFF_HAND(1 << 17),
    SUB_OFF_HAND(1 << 18),

    // combo
    MAIN_OR_SUB(MAIN_HAND._id | SUB_HAND._id, true),
    // 3
    EARRING_RIGHT_OR_LEFT(EARRINGS_LEFT._id | EARRINGS_RIGHT._id, true),
    // 192
    RING_RIGHT_OR_LEFT(RING_LEFT._id | RING_RIGHT._id, true),
    // 768
    SHARD_RIGHT_OR_LEFT(POWER_SHARD_LEFT._id | POWER_SHARD_RIGHT._id, true),
    // 24576

    // STIGMA slots
    STIGMA1(1 << 19),
    STIGMA2(1 << 20),
    STIGMA3(1 << 21),
    STIGMA4(1 << 22),
    STIGMA5(1 << 23),
    STIGMA6(1 << 24),

    REGULAR_STIGMAS(STIGMA1._id | STIGMA2._id | STIGMA3._id | STIGMA4._id | STIGMA5._id | STIGMA6._id, true),

    NONE(1 << 25),
    // Unknown

    ADV_STIGMA1(1 << 26),
    ADV_STIGMA2(1 << 27),
    ADV_STIGMA3(1 << 28),
    ADV_STIGMA4(1 << 29),
    ADV_STIGMA5(1 << 30),
    ADV_STIGMA6(1 << 31),

    ADVANCED_STIGMAS(ADV_STIGMA1._id | ADV_STIGMA2._id | ADV_STIGMA3._id | ADV_STIGMA4._id | ADV_STIGMA5._id | ADV_STIGMA6._id, true),
    ALL_STIGMA(REGULAR_STIGMAS._id | ADVANCED_STIGMAS._id, true);

    private final int _id;
    private final boolean _combo;

    private ItemSlot(int mask) {
        this(mask, false);
    }

    private ItemSlot(int mask, boolean combo) {
        _id = mask;
        _combo = combo;
    }

    public int id() {
        return _id;
    }

    public boolean isCombo() {
        return _combo;
    }

    public static boolean isAdvancedStigma(int slot) {
        return (ADVANCED_STIGMAS._id & slot) == slot;
    }

    public static boolean isRegularStigma(int slot) {
        return (REGULAR_STIGMAS._id & slot) == slot;
    }

    public static boolean isStigma(int slot) {
        return (ALL_STIGMA._id & slot) == slot;
    }

    private static final ItemSlot[] EMPTY = new ItemSlot[0];

    public static ItemSlot[] getSlotsFor(int slotId) {
        if (slotId == 0)
            return EMPTY;

        List<ItemSlot> slots = new ArrayList<>();
        for (ItemSlot slot : values()) {
            if (satisfies(slotId, slot)) {
                slots.add(slot);
            }
        }
        return slots.toArray(new ItemSlot[slots.size()]);
    }

    public static ItemSlot getSlotFor(int slotId) {
        for (ItemSlot slot : values()) {
            if (satisfies(slotId, slot)) {
                return slot;
            }
        }
        throw new IllegalArgumentException("Invalid provided id " + slotId);
    }

    private static boolean satisfies(int slotId, ItemSlot slot) {
        return !slot.isCombo() && (slotId & slot._id) == slot._id;
    }
}
