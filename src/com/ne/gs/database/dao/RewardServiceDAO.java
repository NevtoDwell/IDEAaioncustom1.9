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

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.templates.rewards.RewardEntryItem;

/**
 * @author KID
 */
public abstract class RewardServiceDAO implements DAO {

    @Override
    public final String getClassName() {
        return RewardServiceDAO.class.getName();
    }

    public abstract List<RewardEntryItem> getAvailable(int playerId);

    public abstract void uncheckAvailable(List<Integer> ids);
}
