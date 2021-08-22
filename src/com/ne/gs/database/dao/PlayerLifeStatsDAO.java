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
 * @author Mr. Poke
 */
public abstract class PlayerLifeStatsDAO implements DAO {

    /**
     * Returns unique identifier for PlayerLifeStatsDAO
     *
     * @return unique identifier for PlayerLifeStatsDAO
     */
    @Override
    public final String getClassName() {
        return PlayerLifeStatsDAO.class.getName();
    }

    /**
     * @param player
     */
    public abstract void loadPlayerLifeStat(Player player);

    /**
     * @param player
     */
    public abstract void insertPlayerLifeStat(Player player);

    /**
     * @param player
     */
    public abstract void updatePlayerLifeStat(Player player);

}
