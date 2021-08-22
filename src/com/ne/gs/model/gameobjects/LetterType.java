/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

public enum LetterType {
    NORMAL(0),
    EXPRESS(1),
    BLACKCLOUD(2);

    private final int id;

    private LetterType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LetterType getLetterTypeById(int id) {
        for (LetterType lt : values()) {
            if (lt.id == id) {
                return lt;
            }
        }
        throw new IllegalArgumentException("Unsupported revive type: " + id);
    }
}
