/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.zone;


/**
 * @author MrPoke
 */
public enum ZoneType {

    FLY(0),
    DAMAGE(1),
    WATER(2),
    SIEGE(3),
    PVP(4),
    NEUTRAL(5);

    private final byte value;

    /**
     * @param value
     */
    private ZoneType(int value) {
        this.value = (byte) value;
    }

    /**
     * @return the value
     */
    public byte getValue() {
        return value;
    }
}
