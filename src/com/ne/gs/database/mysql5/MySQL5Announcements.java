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
import com.ne.commons.database.ReadStH;
import com.ne.gs.database.dao.AnnouncementsDAO;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.model.Announcement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Divinity
 */
public class MySQL5Announcements extends AnnouncementsDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Announcement> getAnnouncements() {
        final Set<Announcement> result = new HashSet<>();
        DB.select("SELECT * FROM announcements ORDER BY id", new ReadStH() {

            @Override
            public void handleRead(ResultSet resultSet) throws SQLException {
                while (resultSet.next()) {
                    result.add(new Announcement(resultSet.getInt("id"), resultSet.getString("announce"), resultSet.getString("faction"), resultSet
                        .getString("type"), resultSet.getString("delay")));
                }
            }
        });
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAnnouncement(final Announcement announce) {
        DB.insertUpdate("INSERT INTO announcements (announce, faction, type, delay) VALUES (?, ?, ?, ?)", new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, announce.getAnnounce());
                preparedStatement.setString(2, announce.getFaction());
                preparedStatement.setString(3, announce.getType());
                preparedStatement.setString(4, announce.getDelay());
                preparedStatement.execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delAnnouncement(final int idAnnounce) {
        return DB.insertUpdate("DELETE FROM announcements WHERE id = ?", new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setInt(1, idAnnounce);
                preparedStatement.execute();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String s, int i, int i1) {
        return MySQL5DAOUtils.supports(s, i, i1);
    }
}
