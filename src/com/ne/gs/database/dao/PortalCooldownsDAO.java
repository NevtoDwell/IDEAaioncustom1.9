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

public abstract class PortalCooldownsDAO implements DAO {

    /**
     * Returns unique identifier for PortalCooldownsDAO
     *
     * @return unique identifier for PortalCooldownsDAO
     */
    @Override
    public final String getClassName() {
        return PortalCooldownsDAO.class.getName();
    }

    /**
     * @param player
     */
    public abstract void loadPortalCooldowns(Player player);

    /**
     * @param player
     */
    public abstract void storePortalCooldowns(Player player);

}
