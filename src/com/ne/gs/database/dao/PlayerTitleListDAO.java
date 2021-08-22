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
import com.ne.gs.model.gameobjects.player.title.Title;
import com.ne.gs.model.gameobjects.player.title.TitleList;

/**
 * @author xavier
 */
public abstract class PlayerTitleListDAO implements DAO {

    @Override
    public final String getClassName() {
        return PlayerTitleListDAO.class.getName();
    }

    public abstract TitleList loadTitleList(int playerId);

    public abstract boolean storeTitles(Player player, Title entry);

    public abstract boolean removeTitle(int playerId, int titleId);

}
