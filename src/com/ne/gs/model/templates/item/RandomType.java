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

@XmlEnum
public enum RandomType {

    ENCHANTMENT,
    MANASTONE,
    MANASTONE_COMMON_GRADE_10(10),
    MANASTONE_COMMON_GRADE_20(20),
    MANASTONE_COMMON_GRADE_30(30),
    MANASTONE_COMMON_GRADE_40(40),
    MANASTONE_COMMON_GRADE_50(50),
    MANASTONE_COMMON_GRADE_60(60),
    MANASTONE_RARE_GRADE_10(10),
    MANASTONE_RARE_GRADE_20(20),
    MANASTONE_RARE_GRADE_30(30),
    MANASTONE_RARE_GRADE_40(40),
    MANASTONE_RARE_GRADE_50(50),
    MANASTONE_RARE_GRADE_60(60),
    ANCIENTITEMS,
    CHUNK_EARTH,
    CHUNK_ROCK,
    CHUNK_SAND,
    CHUNK_GEMSTONE;
    private int level;

    private RandomType() {
    }

    private RandomType(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
