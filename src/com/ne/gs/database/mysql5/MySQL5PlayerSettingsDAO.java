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
import com.ne.gs.database.dao.PlayerSettingsDAO;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ATracer
 */
public class MySQL5PlayerSettingsDAO extends PlayerSettingsDAO {

    private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerSettingsDAO.class);

    /**
     * TODO 1) analyze possibility to zip settings 2) insert/update instead of replace 0 - uisettings 1 - shortcuts 2 -
     * display 3 - deny
     */
    @Override
    public void loadSettings(Player player) {
        int playerId = player.getObjectId();
        PlayerSettings playerSettings = new PlayerSettings();
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM player_settings WHERE player_id = ?");
            statement.setInt(1, playerId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int type = resultSet.getInt("settings_type");
                switch (type) {
                    case 0:
                        playerSettings.setUiSettings(resultSet.getBytes("settings"));
                        break;
                    case 1:
                        playerSettings.setShortcuts(resultSet.getBytes("settings"));
                        break;
                    case 2:
                        playerSettings.setDisplay(resultSet.getInt("settings"));
                        break;
                    case 3:
                        playerSettings.setDeny(resultSet.getInt("settings"));
                        break;
                }
            }
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            log.error("Could not restore PlayerSettings data for player " + playerId + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
        playerSettings.setPersistentState(PersistentState.UPDATED);
        player.setPlayerSettings(playerSettings);
    }

    @Override
    public void saveSettings(Player player) {
        final int playerId = player.getObjectId();

        PlayerSettings playerSettings = player.getPlayerSettings();
        if (playerSettings.getPersistentState() == PersistentState.UPDATED) {
            return;
        }

        final byte[] uiSettings = playerSettings.getUiSettings();
        final byte[] shortcuts = playerSettings.getShortcuts();
        final int display = playerSettings.getDisplay();
        final int deny = playerSettings.getDeny();

        if (uiSettings != null) {
            DB.insertUpdate("REPLACE INTO player_settings values (?, ?, ?)", new IUStH() {

                @Override
                public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                    stmt.setInt(1, playerId);
                    stmt.setInt(2, 0);
                    stmt.setBytes(3, uiSettings);
                    stmt.execute();
                }
            });
        }

        if (shortcuts != null) {
            DB.insertUpdate("REPLACE INTO player_settings values (?, ?, ?)", new IUStH() {

                @Override
                public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                    stmt.setInt(1, playerId);
                    stmt.setInt(2, 1);
                    stmt.setBytes(3, shortcuts);
                    stmt.execute();
                }
            });
        }

        DB.insertUpdate("REPLACE INTO player_settings values (?, ?, ?)", new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, playerId);
                stmt.setInt(2, 2);
                stmt.setInt(3, display);
                stmt.execute();
            }
        });

        DB.insertUpdate("REPLACE INTO player_settings values (?, ?, ?)", new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, playerId);
                stmt.setInt(2, 3);
                stmt.setInt(3, deny);
                stmt.execute();
            }
        });

    }

    @Override
    public boolean supports(String databaseName, int majorVersion, int minorVersion) {
        return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
    }
}
