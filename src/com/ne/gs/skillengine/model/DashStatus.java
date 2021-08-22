/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.model;

/**
 * @author weiwei
 * @modified VladimirZ
 */
public enum DashStatus {
    NONE(0),
    RANDOMMOVELOC(1),
    DASH(2),
    BACKDASH(3),
    MOVEBEHIND(4);

    private final int id;

    private DashStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
