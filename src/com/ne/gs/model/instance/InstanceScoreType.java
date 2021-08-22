/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.instance;

/**
 * @author xTz
 */
public enum InstanceScoreType {
    PREPARING(1 * 1024 * 1024),
    // 1048576
    START_PROGRESS(2 * 1024 * 1024),
    // 2097152
    END_PROGRESS(3 * 1024 * 1024); // 3145728

    private final int id;

    private InstanceScoreType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isPreparing() {
        return id == 1048576;
    }

    public boolean isStartProgress() {
        return id == 2097152;
    }

    public boolean isEndProgress() {
        return id == 3145728;
    }
}
