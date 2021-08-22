/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.ArrayList;
import java.util.Map;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.AbyssRankingResult;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.AbyssRank;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.stats.AbyssRankEnum;

/**
 * @author ATracer
 */
public abstract class AbyssRankDAO implements DAO {

    @Override
    public final String getClassName() {
        return AbyssRankDAO.class.getName();
    }

    public abstract void loadAbyssRank(Player player);

    public abstract AbyssRank loadAbyssRank(int playerId);

    public abstract boolean storeAbyssRank(Player player);

    public abstract ArrayList<AbyssRankingResult> getAbyssRankingPlayers(Race race);

    public abstract ArrayList<AbyssRankingResult> getAbyssRankingLegions(Race race);

    public abstract Map<Integer, Integer> loadPlayersAp(Race race, int lowerApLimit);

    public abstract void updateAbyssRank(int playerId, AbyssRankEnum rankEnum);

    public abstract void updateRankList();
}
