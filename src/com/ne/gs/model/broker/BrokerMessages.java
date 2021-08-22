/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.broker;

/**
 * @author kosyachok
 */
public enum BrokerMessages {
    CANT_REGISTER_ITEM(2),
    NO_SPACE_AVAIABLE(3),
    NO_ENOUGHT_KINAH(5);

    private final int id;

    private BrokerMessages(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
