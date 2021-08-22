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
import com.ne.commons.database.IUStH;
import com.ne.commons.database.ParamReadStH;
import com.ne.gs.database.dao.BlockListDAO;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.database.GDB;
import com.ne.gs.model.gameobjects.player.BlockList;
import com.ne.gs.model.gameobjects.player.BlockedPlayer;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ben
 */
public class MySQL5BlockListDAO extends BlockListDAO {

    public static final String LOAD_QUERY = "SELECT blocked_player, reason FROM blocks WHERE player=?";
    public static final String ADD_QUERY = "INSERT INTO blocks (player, blocked_player, reason) VALUES (?, ?, ?)";
    public static final String DEL_QUERY = "DELETE FROM blocks WHERE player=? AND blocked_player=?";
    public static final String SET_REASON_QUERY = "UPDATE blocks SET reason=? WHERE player=? AND blocked_player=?";
    private static final Logger log = LoggerFactory.getLogger(MySQL5BlockListDAO.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addBlockedUser(final int playerObjId, final int objIdToBlock, final String reason) {
        return DB.insertUpdate(ADD_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, playerObjId);
                stmt.setInt(2, objIdToBlock);
                stmt.setString(3, reason);
                stmt.execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delBlockedUser(final int playerObjId, final int objIdToDelete) {
        return DB.insertUpdate(DEL_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, playerObjId);
                stmt.setInt(2, objIdToDelete);
                stmt.execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockList load(final Player player) {
        final Map<Integer, BlockedPlayer> list = new HashMap<>();

        DB.select(LOAD_QUERY, new ParamReadStH() {

            @Override
            public void handleRead(ResultSet rset) throws SQLException {
                PlayerDAO playerDao = GDB.get(PlayerDAO.class);
                while (rset.next()) {
                    int blockedOid = rset.getInt("blocked_player");
                    PlayerCommonData pcd = playerDao.loadPlayerCommonData(blockedOid);
                    if (pcd == null) {
                        log.error("Attempt to load block list for " + player.getName() + " tried to load a player which does not exist: " + blockedOid);
                    } else {
                        list.put(blockedOid, new BlockedPlayer(pcd, rset.getString("reason")));
                    }
                }

            }

            @Override
            public void setParams(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, player.getObjectId());
            }
        });
        return new BlockList(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setReason(final int playerObjId, final int blockedPlayerObjId, final String reason) {
        return DB.insertUpdate(SET_REASON_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, reason);
                stmt.setInt(2, playerObjId);
                stmt.setInt(3, blockedPlayerObjId);
                stmt.execute();

            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String databaseName, int majorVersion, int minorVersion) {
        return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
    }
}
