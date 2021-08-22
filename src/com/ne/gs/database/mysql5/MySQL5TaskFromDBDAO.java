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
import com.ne.gs.database.dao.TaskFromDBDAO;
import com.ne.gs.model.tasks.TaskFromDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author Divinity
 */
public class MySQL5TaskFromDBDAO extends TaskFromDBDAO {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(MySQL5TaskFromDBDAO.class);
    private static final String SELECT_ALL_QUERY = "SELECT * FROM tasks ORDER BY id";
    private static final String UPDATE_QUERY_LAST_ACTIVATION = "UPDATE tasks SET last_activation = ? WHERE id = ?";
    private static final String UPDATE_QUERY_START_TIME = "UPDATE tasks SET start_time = ? WHERE id = ?";
    private static final String SELECT_NAME_NPCID_QUERY = "SELECT * FROM tasks WHERE task = ? AND param LIKE ?";

    @Override
    public ArrayList<TaskFromDB> getAllTasks() {
        ArrayList<TaskFromDB> result = new ArrayList<>();

        Connection con = null;

        PreparedStatement stmt = null;
        try {
            con = DatabaseFactory.getConnection();
            stmt = con.prepareStatement(SELECT_ALL_QUERY);

            ResultSet rset = stmt.executeQuery();

            while (rset.next()) {
                result.add(new TaskFromDB(rset.getInt("id"), rset.getString("task"), rset.getString("type"), rset.getTimestamp("last_activation"), rset
                    .getString("start_time"), rset.getInt("delay"), rset.getString("param")));
            }

            rset.close();
            stmt.close();
        } catch (SQLException e) {
            log.error("getAllTasks", e);
        } finally {
            DatabaseFactory.close(stmt, con);
        }

        return result;
    }

    @Override
    public ArrayList<TaskFromDB> getTasksByNameAndNpcId(String name, int npcId) {
        ArrayList<TaskFromDB> result = new ArrayList<>();

        Connection con = null;

        PreparedStatement stmt = null;
        try {
            con = DatabaseFactory.getConnection();
            stmt = con.prepareStatement(SELECT_NAME_NPCID_QUERY);

            stmt.setString(1, name);
            String npc = String.valueOf(npcId) + "%";
            stmt.setString(2, npc);

            ResultSet rset = stmt.executeQuery();

            while (rset.next()) {
                result.add(new TaskFromDB(rset.getInt("id"), rset.getString("task"), rset.getString("type"), rset.getTimestamp("last_activation"), rset
                        .getString("start_time"), rset.getInt("delay"), rset.getString("param")));
            }

            rset.close();
            stmt.close();
        } catch (SQLException e) {
            log.error("getTasksByNameAndNpcId", e);
        } finally {
            DatabaseFactory.close(stmt, con);
        }

        return result;
    }

    @Override
    public void setLastActivation(int id) {
        Connection con = null;

        PreparedStatement stmt = null;
        try {
            con = DatabaseFactory.getConnection();
            stmt = con.prepareStatement(UPDATE_QUERY_LAST_ACTIVATION);

            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(2, id);
            stmt.execute();
        } catch (SQLException e) {
            log.error("setLastActivation", e);
        } finally {
            DatabaseFactory.close(stmt, con);
        }
    }

    @Override
    public void setStartTime(String cron, int id) {
        Connection con = null;

        PreparedStatement stmt = null;
        try {
            con = DatabaseFactory.getConnection();
            stmt = con.prepareStatement(UPDATE_QUERY_START_TIME);

            stmt.setString(1, cron);
            stmt.setInt(2, id);
            stmt.execute();
        } catch (SQLException e) {
            log.error("setStartTime", e);
        } finally {
            DatabaseFactory.close(stmt, con);
        }
    }

    @Override
    public boolean supports(String s, int i, int i1) {
        return MySQL5DAOUtils.supports(s, i, i1);
    }
}
