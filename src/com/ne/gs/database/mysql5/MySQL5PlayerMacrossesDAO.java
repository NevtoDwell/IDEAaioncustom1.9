/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.mysql5;

import com.ne.commons.database.DB;
import com.ne.commons.database.DatabaseFactory;
import com.ne.commons.database.IUStH;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.database.dao.PlayerMacrossesDAO;
import com.ne.gs.model.gameobjects.player.MacroList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aquanox
 */
public class MySQL5PlayerMacrossesDAO extends PlayerMacrossesDAO {

    private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerMacrossesDAO.class);
    public static final String INSERT_QUERY = "INSERT INTO `player_macrosses` (`player_id`, `order`, `macro`) VALUES (?,?,?)";
    public static final String UPDATE_QUERY = "UPDATE `player_macrosses` SET `macro`=? WHERE `player_id`=? AND `order`=?";
    public static final String DELETE_QUERY = "DELETE FROM `player_macrosses` WHERE `player_id`=? AND `order`=?";
    public static final String SELECT_QUERY = "SELECT `order`, `macro` FROM `player_macrosses` WHERE `player_id`=?";

    /**
     * Add a macro information into database
     *
     * @param playerId
     *     player object id
     * @param macro
     *     macro contents.
     */
    @Override
    public void addMacro(final int playerId, final int macroPosition, final String macro) {
        DB.insertUpdate(INSERT_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                log.debug("[DAO: MySQL5PlayerMacrossesDAO] storing macro " + playerId + " " + macroPosition);
                stmt.setInt(1, playerId);
                stmt.setInt(2, macroPosition);
                stmt.setString(3, macro);
                stmt.execute();
            }
        });
    }

    @Override
    public void updateMacro(final int playerId, final int macroPosition, final String macro) {
        DB.insertUpdate(UPDATE_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                log.debug("[DAO: MySQL5PlayerMacrossesDAO] updating macro " + playerId + " " + macroPosition);
                stmt.setString(1, macro);
                stmt.setInt(2, playerId);
                stmt.setInt(3, macroPosition);
                stmt.execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMacro(final int playerId, final int macroPosition) {
        DB.insertUpdate(DELETE_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                log.debug("[DAO: MySQL5PlayerMacrossesDAO] removing macro " + playerId + " " + macroPosition);
                stmt.setInt(1, playerId);
                stmt.setInt(2, macroPosition);
                stmt.execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MacroList restoreMacrosses(int playerId) {
        Map<Integer, String> macrosses = new HashMap<>();
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);
            stmt.setInt(1, playerId);
            ResultSet rset = stmt.executeQuery();
            log.debug("[DAO: MySQL5PlayerMacrossesDAO] loading macroses for playerId: " + playerId);
            while (rset.next()) {
                int order = rset.getInt("order");
                String text = rset.getString("macro");
                macrosses.put(order, text);
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            log.error("Could not restore MacroList data for player " + playerId + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
        return new MacroList(macrosses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String databaseName, int majorVersion, int minorVersion) {
        return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
    }
}
