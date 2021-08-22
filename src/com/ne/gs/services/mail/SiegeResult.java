/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.mail;

public enum SiegeResult {
    DEFENCE(0),
    OCCUPY(1),
    PROTECT(2),
    DEFENDER(3),
    EMPTY(4),
    FAIL(5);

    private final int value;

    private SiegeResult(int value) {
        this.value = value;
    }

    public int getId() {
        return value;
    }
}
