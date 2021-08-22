/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.pet;

/**
 * @author IlBuono, Rolandas
 */
public enum PetFunctionType {
    WAREHOUSE(0, true),
    FOOD(2049, true),
    LOOT(259, true),
    DOPING(8194, true),

    APPEARANCE(1),
    NONE(4, true),

    BAG(-1),
    WING(-2);

    private final short id;
    private boolean isPlayerFunc = false;

    private PetFunctionType(int id, boolean isPlayerFunc) {
        this(id);
        this.isPlayerFunc = isPlayerFunc;
    }

    private PetFunctionType(int id) {
        this.id = (short) (id & 0xFFFF);
    }

    public int getId() {
        return id;
    }

    public boolean isPlayerFunction() {
        return isPlayerFunc;
    }
}
