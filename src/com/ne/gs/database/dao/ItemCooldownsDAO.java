/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public abstract class ItemCooldownsDAO implements DAO {

    /**
     * Returns unique identifier for ItemCooldownsDAO
     *
     * @return unique identifier for ItemCooldownsDAO
     */
    @Override
    public final String getClassName() {
        return ItemCooldownsDAO.class.getName();
    }

    /**
     * @param player
     */
    public abstract void loadItemCooldowns(Player player);

    /**
     * @param player
     */
    public abstract void storeItemCooldowns(Player player);

}
