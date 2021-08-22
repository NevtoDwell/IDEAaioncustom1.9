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
 * @author synchro2
 */
public abstract class CraftCooldownsDAO implements DAO {

    @Override
    public final String getClassName() {
        return CraftCooldownsDAO.class.getName();
    }

    public abstract void loadCraftCooldowns(Player player);

    public abstract void storeCraftCooldowns(Player player);

}
