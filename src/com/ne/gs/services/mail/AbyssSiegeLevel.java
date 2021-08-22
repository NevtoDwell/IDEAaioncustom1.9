/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.mail;

public enum AbyssSiegeLevel {
    NONE(0),
    HERO_DECORATION(1),
    MEDAL(2),
    ELITE_SOLDIER(3),
    VETERAN_SOLDIER(4);

    private final int value;

    private AbyssSiegeLevel(int value) {
        this.value = value;
    }

    public int getId() {
        return value;
    }
     public static AbyssSiegeLevel getLevelById(int id) {
        for (AbyssSiegeLevel al : values()) {
            if (al.getId() == id) {
                return al;
            }
        }
        throw new IllegalArgumentException("There is no AbyssSiegeLevel with ID " + id);
    }
}
