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
public abstract class PlayerEffectsDAO implements DAO {

    /**
     * Returns unique identifier for PlayerEffectsDAO
     *
     * @return unique identifier for PlayerEffectsDAO
     */
    @Override
    public final String getClassName() {
        return PlayerEffectsDAO.class.getName();
    }

    /**
     * @param player
     */
    public abstract void loadPlayerEffects(Player player);

    /**
     * @param player
     */
    public abstract void storePlayerEffects(Player player);

}
