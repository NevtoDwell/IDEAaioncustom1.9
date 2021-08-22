/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

/**
 * @author MrPoke
 */
public final class DescId {

    private final int _value;

    public DescId(int value) {
        _value = value;
    }

    public int getValue() {
        return _value;
    }

    public static DescId of(int value) {
        return new DescId(value);
    }
}
