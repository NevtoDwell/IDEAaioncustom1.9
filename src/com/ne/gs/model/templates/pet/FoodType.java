/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.pet;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "FoodType")
@XmlEnum
public enum FoodType {
    AETHER_CRYSTAL_BISCUIT,
    AETHER_GEM_BISCUIT,
    AETHER_POWDER_BISCUIT,
    ARMOR,
    BALAUR_SCALES,
    BONES,
    EXCLUDES,
    FLUIDS,
    HEALTHY_FOOD_ALL,
    HEALTHY_FOOD_SPICY,
    MISCELLANEOUS,
    POPPY_SNACK,
    POPPY_SNACK_TASTY,
    POPPY_SNACK_NUTRITIOUS,
    SOULS,
    STINKY,
    THORNS;

    public String value() {
        return name();
    }

    public static FoodType fromValue(String value) {
        return valueOf(value);
    }
}
