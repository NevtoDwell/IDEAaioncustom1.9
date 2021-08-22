/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

public enum EventType {
    NONE(0, ""),
    CHRISTMAS(1, "christmas"),
    HALLOWEEN(2, "halloween"),
    VALENTINE(4, "valentine");

    private final int id;
    private final String theme;

    private EventType(int id, String theme) {
        this.id = id;
        this.theme = theme;
    }

    public int getId() {
        return id;
    }

    public String getTheme() {
        return theme;
    }

    public static EventType getEventType(String theme) {
        for (EventType type : values()) {
            if (theme.equals(type.getTheme())) {
                return type;
            }
        }
        return NONE;
    }
}
