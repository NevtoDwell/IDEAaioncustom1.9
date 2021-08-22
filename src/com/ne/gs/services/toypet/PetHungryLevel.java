/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.toypet;

public enum PetHungryLevel {
    HUNGRY(0),
    CONTENT(1),
    SEMIFULL(2),
    FULL(3);

    private byte value;

    private PetHungryLevel(int value) {
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }

    public PetHungryLevel getNextValue() {
        byte levelValue = value;
        switch (levelValue) {
            case 0:
                return CONTENT;
            case 1:
                return SEMIFULL;
            case 2:
                return FULL;
            case 3:
                return HUNGRY;
        }
        return HUNGRY;
    }

    public static PetHungryLevel fromId(int value) {
        return values()[value];
    }
}
