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
import com.ne.gs.database.dao.MotionDAO;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.database.dao.PlayerEmotionListDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.motion.Motion;
import com.ne.gs.model.gameobjects.player.motion.MotionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author MrPoke
 */
public class MySQL5MotionDAO extends MotionDAO {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(PlayerEmotionListDAO.class);
    public static final String INSERT_QUERY = "INSERT INTO `player_motions` (`player_id`, `motion_id`, `active`,  `time`) VALUES (?,?,?,?)";
    public static final String SELECT_QUERY = "SELECT `motion_id`, `active`, `time` FROM `player_motions` WHERE `player_id`=?";
    public static final String DELETE_QUERY = "DELETE FROM `player_motions` WHERE `player_id`=? AND `motion_id`=?";
    public static final String UPDATE_QUERY = "UPDATE `player_motions` SET `active`=? WHERE `player_id`=? AND `motion_id`=?";

    @Override
    public boolean supports(String s, int i, int i1) {
        return MySQL5DAOUtils.supports(s, i, i1);
    }

    @Override
    public void loadMotionList(Player player) {
        Connection con = null;
        MotionList motions = new MotionList(player);
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);
            stmt.setInt(1, player.getObjectId());
            ResultSet rset = stmt.executeQuery();
            while (rset.next()) {
                int motionId = rset.getInt("motion_id");
                int time = rset.getInt("time");
                boolean isActive = rset.getBoolean("active");
                motions.add(new Motion(motionId, time, isActive), false);
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not restore motions for playerObjId: " + player.getObjectId() + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
        player.setMotions(motions);
    }

    @Override
    public boolean storeMotion(int objectId, Motion motion) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(INSERT_QUERY);
            stmt.setInt(1, objectId);
            stmt.setInt(2, motion.getId());
            stmt.setBoolean(3, motion.isActive());
            stmt.setInt(4, motion.getExpireTime());
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not store motion for player " + objectId + " from GDB: " + e.getMessage(), e);
            return false;
        } finally {
            DatabaseFactory.close(con);
        }
        return true;
    }

    @Override
    public boolean deleteMotion(int objectId, int motionId) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(DELETE_QUERY);
            stmt.setInt(1, objectId);
            stmt.setInt(2, motionId);
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not delete motion for player " + objectId + " from GDB: " + e.getMessage(), e);
            return false;
        } finally {
            DatabaseFactory.close(con);
        }
        return true;
    }

    @Override
    public boolean updateMotion(int objectId, Motion motion) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY);
            stmt.setBoolean(1, motion.isActive());
            stmt.setInt(2, objectId);
            stmt.setInt(3, motion.getId());
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not store motion for player " + objectId + " from GDB: " + e.getMessage(), e);
            return false;
        } finally {
            DatabaseFactory.close(con);
        }
        return true;
    }
}
