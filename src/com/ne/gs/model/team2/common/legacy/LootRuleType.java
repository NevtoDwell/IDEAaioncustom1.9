/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.common.legacy;

/**
 * @author Lyahim
 */
public enum LootRuleType {

    FREEFORALL(0),
    ROUNDROBIN(1),
    LEADER(2);

    private final int id;

    private LootRuleType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
