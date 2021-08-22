/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.mysql5;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.database.DB;
import com.ne.commons.database.DatabaseFactory;
import com.ne.commons.database.IUStH;
import com.ne.commons.database.ParamReadStH;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.gs.database.dao.ItemCooldownsDAO;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.model.gameobjects.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author ATracer
 */
public class MySQL5ItemCooldownsDAO extends ItemCooldownsDAO {

    private static final Logger log = LoggerFactory.getLogger(MySQL5ItemCooldownsDAO.class);

    public static final String INSERT_QUERY = "INSERT INTO `item_cooldowns` (`player_id`, `delay_id`, `use_delay`, `reuse_time`) VALUES (?,?,?,?)";
    public static final String DELETE_QUERY = "DELETE FROM `item_cooldowns` WHERE `player_id`=?";
    public static final String SELECT_QUERY = "SELECT `delay_id`, `use_delay`, `reuse_time` FROM `item_cooldowns` WHERE `player_id`=?";

    private static final Predicate<Tuple2<Long, Integer>> itemCooldownPredicate = new Predicate<Tuple2<Long, Integer>>() {
        @Override
        public boolean apply(@Nullable Tuple2<Long, Integer> input) {
            return input != null && input._1 - System.currentTimeMillis() > 30000;
        }
    };

    @Override
    public void loadItemCooldowns(final Player player) {
        DB.select(SELECT_QUERY, new ParamReadStH() {

            @Override
            public void setParams(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, player.getObjectId());
            }

            @Override
            public void handleRead(ResultSet rset) throws SQLException {
                while (rset.next()) {
                    int delayId = rset.getInt("delay_id");
                    int useDelay = rset.getInt("use_delay");
                    long reuseTime = rset.getLong("reuse_time");

                    if (reuseTime > System.currentTimeMillis()) {
                        player.addItemCoolDown(delayId, reuseTime, useDelay);
                    }

                }
            }
        });
        player.getEffectController().broadCastEffects();
    }

    @Override
    public void storeItemCooldowns(Player player) {
        deleteItemCooldowns(player);
        Map<Integer, Tuple2<Long, Integer>> itemCoolDowns = player.getItemCoolDowns();
        Map<Integer, Tuple2<Long, Integer>> map = Maps.filterValues(itemCoolDowns, itemCooldownPredicate);
        if (map.size() == 0) {
            return;
        }

        Connection con = null;
        PreparedStatement st = null;
        try {
            con = DatabaseFactory.getConnection();
            con.setAutoCommit(false);
            st = con.prepareStatement(INSERT_QUERY);

            for (Map.Entry<Integer, Tuple2<Long, Integer>> e : map.entrySet()) {
                st.setInt(1, player.getObjectId());
                st.setInt(2, e.getKey());
                st.setInt(3, e.getValue()._2);
                st.setLong(4, e.getValue()._1);
                st.addBatch();
            }

            st.executeBatch();
            con.commit();
        } catch (SQLException e) {
            log.error("Error while storing item cooldows for player " + player.getObjectId(), e);
        } finally {
            DatabaseFactory.close(st, con);
        }
    }

    private void deleteItemCooldowns(final Player player) {
        DB.insertUpdate(DELETE_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, player.getObjectId());
                stmt.execute();
            }
        });
    }

    @Override
    public boolean supports(String arg0, int arg1, int arg2) {
        return MySQL5DAOUtils.supports(arg0, arg1, arg2);
    }
}
