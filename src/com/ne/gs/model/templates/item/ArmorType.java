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
@XmlType(name = "armor_type")
@XmlEnum
public enum ArmorType {
    CHAIN(new int[]{6, 13}),
    CLOTHES(new int[]{4}),
    LEATHER(new int[]{5, 12}),
    PLATE(new int[]{18}),
    ROBE(new int[]{67, 70}),
    SHARD(new int[]{}),
    SHIELD(new int[]{7, 14}),
    ARROW(new int[]{});

    private final int[] requiredSkills;

    private ArmorType(int[] requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public int[] getRequiredSkills() {
        return requiredSkills;
    }

    /**
     * @return int
     */
    public int getMask() {
        return 1 << this.ordinal();
    }
}
