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
import com.ne.gs.database.dao.RewardServiceDAO;
import com.ne.gs.model.templates.rewards.RewardEntryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author KID
 */
public class MySQL5RewardServiceDAO extends RewardServiceDAO {

    private static final Logger log = LoggerFactory.getLogger(MySQL5RewardServiceDAO.class);
    public static final String UPDATE_QUERY = "UPDATE `web_reward` SET `rewarded`=?, received=NOW() WHERE `unique`=?";
    public static final String SELECT_QUERY = "SELECT * FROM `web_reward` WHERE `item_owner`=? AND `rewarded`=?";

    @Override
    public boolean supports(String arg0, int arg1, int arg2) {
        return MySQL5DAOUtils.supports(arg0, arg1, arg2);
    }

    @Override
    public List<RewardEntryItem> getAvailable(int playerId) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);
            stmt.setInt(1, playerId);
            stmt.setInt(2, 0);

            List<RewardEntryItem> list = new ArrayList<>();
            ResultSet rset = stmt.executeQuery();
            while (rset.next()) {
                int unique = rset.getInt("unique");
                int item_id = rset.getInt("item_id");
                long count = rset.getLong("item_count");
                list.add(new RewardEntryItem(unique, item_id, count));
            }
            rset.close();
            stmt.close();

            return list;
        } catch (Exception e) {
            log.warn("getAvailable() for " + playerId + " from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }

        return Collections.emptyList();
    }

    @Override
    public void uncheckAvailable(List<Integer> ids) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt;
            for (int uniqid : ids) {
                stmt = con.prepareStatement(UPDATE_QUERY);
                stmt.setInt(1, 1);
                stmt.setInt(2, uniqid);
                stmt.execute();
                stmt.close();
            }
        } catch (Exception e) {
            log.error("uncheckAvailable", e);
        } finally {
            DatabaseFactory.close(con);
        }
    }
}
