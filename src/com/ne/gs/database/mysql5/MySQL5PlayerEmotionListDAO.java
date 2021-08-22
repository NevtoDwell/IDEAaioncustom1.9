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
import com.ne.gs.database.dao.PlayerEmotionListDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.emotion.Emotion;
import com.ne.gs.model.gameobjects.player.emotion.EmotionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Mr. Poke
 */
public class MySQL5PlayerEmotionListDAO extends PlayerEmotionListDAO {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(PlayerEmotionListDAO.class);
    public static final String INSERT_QUERY = "INSERT INTO `player_emotions` (`player_id`, `emotion`, `remaining`) VALUES (?,?,?)";
    public static final String SELECT_QUERY = "SELECT `emotion`, `remaining` FROM `player_emotions` WHERE `player_id`=?";
    public static final String DELETE_QUERY = "DELETE FROM `player_emotions` WHERE `player_id`=? AND `emotion`=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String databaseName, int majorVersion, int minorVersion) {
        return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
    }

    @Override
    public void loadEmotions(Player player) {
        EmotionList emotions = new EmotionList(player);
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);
            stmt.setInt(1, player.getObjectId());
            ResultSet rset = stmt.executeQuery();
            while (rset.next()) {
                int emotionId = rset.getInt("emotion");
                int remaining = rset.getInt("remaining");
                emotions.add(emotionId, remaining, false);
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not restore emotionId for playerObjId: " + player.getObjectId() + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
        player.setEmotions(emotions);
    }

    @Override
    public void insertEmotion(Player player, Emotion emotion) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(INSERT_QUERY);
            stmt.setInt(1, player.getObjectId());
            stmt.setInt(2, emotion.getId());
            stmt.setInt(3, emotion.getExpireTime());
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not store emotionId for player " + player.getObjectId() + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
    }

    @Override
    public void deleteEmotion(int playerId, int emotionId) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(DELETE_QUERY);
            stmt.setInt(1, playerId);
            stmt.setInt(2, emotionId);
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not delete title for player " + playerId + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
    }
}
