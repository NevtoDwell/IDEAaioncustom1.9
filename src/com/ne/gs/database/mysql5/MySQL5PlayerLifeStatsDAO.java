/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.mysql5;

import com.ne.commons.database.DatabaseFactory;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.database.dao.PlayerLifeStatsDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.PlayerLifeStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Mr. Poke
 */
public class MySQL5PlayerLifeStatsDAO extends PlayerLifeStatsDAO {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerLifeStatsDAO.class);
    public static final String INSERT_QUERY = "INSERT INTO `player_life_stats` (`player_id`, `hp`, `mp`, `fp`, `dp`) VALUES (?,?,?,?,?)";
    public static final String SELECT_QUERY = "SELECT `hp`, `mp`, `fp`, `dp` FROM `player_life_stats` WHERE `player_id`=?";
    public static final String UPDATE_QUERY = "UPDATE player_life_stats set `hp`=?, `mp`=?, `fp`=?, `dp`=? WHERE `player_id`=?";

    @Override
    public void loadPlayerLifeStat(Player player) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);
            stmt.setInt(1, player.getObjectId());
            ResultSet rset = stmt.executeQuery();
            if (rset.next()) {
                PlayerLifeStats lifeStats = player.getLifeStats();
                lifeStats.setCurrentHp(rset.getInt("hp"));
                lifeStats.setCurrentMp(rset.getInt("mp"));
                lifeStats.setCurrentFp(rset.getInt("fp"));
	            lifeStats.setCurrentDp(rset.getInt("dp"));
            } else {
                insertPlayerLifeStat(player);
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not restore PlayerLifeStat data for playerObjId: " + player.getObjectId() + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
    }

    @Override
    public void insertPlayerLifeStat(Player player) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(INSERT_QUERY);
            stmt.setInt(1, player.getObjectId());
            stmt.setInt(2, player.getLifeStats().getCurrentHp());
            stmt.setInt(3, player.getLifeStats().getCurrentMp());
            stmt.setInt(4, player.getLifeStats().getCurrentFp());
	        stmt.setInt(5, player.getLifeStats().getCurrentDp());
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not store PlayerLifeStat data for player " + player.getObjectId() + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
    }

    @Override
    public void updatePlayerLifeStat(Player player) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY);
            stmt.setInt(1, player.getLifeStats().getCurrentHp());
            stmt.setInt(2, player.getLifeStats().getCurrentMp());
            stmt.setInt(3, player.getLifeStats().getCurrentFp());
            stmt.setInt(4, player.getLifeStats().getCurrentDp());
            stmt.setInt(5, player.getObjectId());
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not update PlayerLifeStat data for player " + player.getObjectId() + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
    }

    @Override
    public boolean supports(String databaseName, int majorVersion, int minorVersion) {
        return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
    }
}
