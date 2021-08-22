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

public abstract class WeddingDAO implements DAO {

    @Override
    public final String getClassName() {
        return WeddingDAO.class.getName();
    }

    public abstract int loadPartnerId(Player player);

    public abstract void storeWedding(Player partner1, Player partner2);

    public abstract void deleteWedding(Player paramPlayer1, Player paramPlayer2);
}
