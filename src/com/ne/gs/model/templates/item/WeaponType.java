/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "weapon_type")
@XmlEnum
public enum WeaponType {
    DAGGER_1H(new int[]{30, 9}, 1),
    MACE_1H(new int[]{3, 10}, 1),
    SWORD_1H(new int[]{1, 8}, 1),
    TOOLHOE_1H(new int[]{}, 1),
    BOOK_2H(new int[]{64}, 2),
    ORB_2H(new int[]{64}, 2),
    POLEARM_2H(new int[]{16}, 2),
    STAFF_2H(new int[]{53}, 2),
    SWORD_2H(new int[]{15}, 2),
    TOOLPICK_2H(new int[]{}, 2),
    TOOLROD_2H(new int[]{}, 2),
    BOW(new int[]{17}, 2);

    private final int[] requiredSkill;
    private final int slots;

    private WeaponType(int[] requiredSkills, int slots) {
        this.requiredSkill = requiredSkills;
        this.slots = slots;
    }

    public int[] getRequiredSkills() {
        return requiredSkill;
    }

    public int getRequiredSlots() {
        return slots;
    }

    /**
     * @return int
     */
    public int getMask() {
        return 1 << this.ordinal();
    }
}
