/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.List;

import com.ne.gs.model.gameobjects.BrokerItem;

public abstract class BrokerDAO implements IDFactoryAwareDAO {

    public abstract List<BrokerItem> loadBroker();

    public abstract boolean store(BrokerItem brokerItem);

    public abstract boolean preBuyCheck(int itemForCheck);

    @Override
    public final String getClassName() {
        return BrokerDAO.class.getName();
    }
}
