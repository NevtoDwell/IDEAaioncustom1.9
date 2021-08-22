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
import com.google.common.collect.Iterators;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.database.DB;
import com.ne.commons.database.DatabaseFactory;
import com.ne.commons.database.IUStH;
import com.ne.commons.database.ParamReadStH;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.database.dao.PlayerEffectsDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Effect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author ATracer
 */
public class MySQL5PlayerEffectsDAO extends PlayerEffectsDAO {

    private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerEffectsDAO.class);

    public static final String INSERT_QUERY = "INSERT INTO `player_effects` (`player_id`, `skill_id`, `skill_lvl`, `current_time`, `end_time`) VALUES (?,?,?,?,?)";
    public static final String DELETE_QUERY = "DELETE FROM `player_effects` WHERE `player_id`=?";
    public static final String SELECT_QUERY = "SELECT `skill_id`, `skill_lvl`, `current_time`, `end_time` FROM `player_effects` WHERE `player_id`=?";

    private static final Predicate<Effect> insertableEffectsPredicate = new Predicate<Effect>() {
        @Override
        public boolean apply(@Nullable Effect input) {
            return input != null && input.getRemainingTime() > 28000;
        }
    };

    @Override
    public void loadPlayerEffects(final Player player) {
        DB.select(SELECT_QUERY, new ParamReadStH() {

            @Override
            public void setParams(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, player.getObjectId());
            }

            @Override
            public void handleRead(ResultSet rset) throws SQLException {
                while (rset.next()) {
                    int skillId = rset.getInt("skill_id");
                    int skillLvl = rset.getInt("skill_lvl");
                    int remainingTime = rset.getInt("current_time");
                    long endTime = rset.getLong("end_time");

                    if (remainingTime > 0) {
                        player.getEffectController().addSavedEffect(skillId, skillLvl, remainingTime, endTime);
                    }
                }
            }
        });
        player.getEffectController().broadCastEffects();
    }

    @Override
    public void storePlayerEffects(Player player) {
        deletePlayerEffects(player);

        Iterator<Effect> iterator = player.getEffectController().iterator();
        iterator = Iterators.filter(iterator, insertableEffectsPredicate);

        if (!iterator.hasNext()) {
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DatabaseFactory.getConnection();
            con.setAutoCommit(false);
            ps = con.prepareStatement(INSERT_QUERY);

            while (iterator.hasNext()) {
                Effect effect = iterator.next();
                ps.setInt(1, player.getObjectId());
                ps.setInt(2, effect.getSkillId());
                ps.setInt(3, effect.getSkillLevel());
                ps.setInt(4, effect.getRemainingTime());
                ps.setLong(5, effect.getEndTime());
                ps.addBatch();
            }

            ps.executeBatch();
            con.commit();
        } catch (SQLException e) {
            log.error("Exception while saving effects of player " + player.getObjectId(), e);
        } finally {
            DatabaseFactory.close(ps, con);
        }
    }

    private void deletePlayerEffects(final Player player) {
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
