/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team.legion;

/**
 * @author cura
 */
public enum LegionEmblemType {
    DEFAULT(0x00),
    CUSTOM(0x80);

    private final byte value;

    private LegionEmblemType(int value) {
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }
}
