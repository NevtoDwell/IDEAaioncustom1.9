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
 * @author nrg
 */
public abstract class PlayerCooldownsDAO implements DAO {

    /**
     * Returns unique identifier for PlayerCooldownsDAO
     *
     * @return unique identifier for PlayerCooldownsDAO
     */
    @Override
    public final String getClassName() {
        return PlayerCooldownsDAO.class.getName();
    }

    /**
     * @param player
     */
    public abstract void loadPlayerCooldowns(Player player);

    /**
     * @param player
     */
    public abstract void storePlayerCooldowns(Player player);

}
