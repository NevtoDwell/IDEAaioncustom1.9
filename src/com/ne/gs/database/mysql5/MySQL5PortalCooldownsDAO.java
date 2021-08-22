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
import com.ne.gs.database.dao.PortalCooldownsDAO;
import com.ne.gs.model.gameobjects.player.Player;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class MySQL5PortalCooldownsDAO extends PortalCooldownsDAO {

    private static final Logger log = LoggerFactory.getLogger(MySQL5PortalCooldownsDAO.class);

    public static final String INSERT_QUERY = "INSERT INTO `portal_cooldowns` (`player_id`, `world_id`, `reuse_time`) VALUES (?,?,?)";
    public static final String DELETE_QUERY = "DELETE FROM `portal_cooldowns` WHERE `player_id`=?";
    public static final String SELECT_QUERY = "SELECT `world_id`, `reuse_time` FROM `portal_cooldowns` WHERE `player_id`=?";

    @Override
    public void loadPortalCooldowns(Player player) {
        Connection con = null;
        FastMap<Integer, Long> portalCoolDowns = new FastMap<>();
        PreparedStatement stmt = null;
        try {
            con = DatabaseFactory.getConnection();
            stmt = con.prepareStatement(SELECT_QUERY);

            stmt.setInt(1, player.getObjectId());
            ResultSet rset = stmt.executeQuery();

            while (rset.next()) {
                int worldId = rset.getInt("world_id");
                long reuseTime = rset.getLong("reuse_time");
                if (reuseTime > System.currentTimeMillis()) {
                    portalCoolDowns.put(worldId, reuseTime);
                }
            }
            player.getPortalCooldownList().setPortalCoolDowns(portalCoolDowns);
            rset.close();
        } catch (SQLException e) {
            log.error("LoadPortalCooldowns", e);
        } finally {
            DatabaseFactory.close(stmt, con);
        }
    }

    @Override
    public void storePortalCooldowns(Player player) {
        deletePortalCooldowns(player);
        Map<Integer, Long> portalCoolDowns = player.getPortalCooldownList().getPortalCoolDowns();

        if (portalCoolDowns == null) {
            return;
        }

        for (Map.Entry<Integer, Long> entry : portalCoolDowns.entrySet()) {
            int worldId = entry.getKey();
            long reuseTime = entry.getValue();

            if (reuseTime < System.currentTimeMillis()) {
                continue;
            }

            Connection con = null;

            PreparedStatement stmt = null;
            try {
                con = DatabaseFactory.getConnection();
                stmt = con.prepareStatement(INSERT_QUERY);

                stmt.setInt(1, player.getObjectId());
                stmt.setInt(2, worldId);
                stmt.setLong(3, reuseTime);
                stmt.execute();
            } catch (SQLException e) {
                log.error("storePortalCooldowns", e);
            } finally {
                DatabaseFactory.close(stmt, con);
            }
        }
    }

    private void deletePortalCooldowns(Player player) {

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = DatabaseFactory.getConnection();
            stmt = con.prepareStatement(DELETE_QUERY);

            stmt.setInt(1, player.getObjectId());
            stmt.execute();
        } catch (SQLException e) {
            log.error("deletePortalCooldowns", e);
        } finally {
            DatabaseFactory.close(stmt, con);
        }
    }

    @Override
    public boolean supports(String arg0, int arg1, int arg2) {
        return MySQL5DAOUtils.supports(arg0, arg1, arg2);
    }
}
