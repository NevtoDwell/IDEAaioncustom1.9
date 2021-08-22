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
import com.ne.gs.database.dao.SurveyControllerDAO;
import com.ne.gs.model.templates.survey.SurveyItem;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author KID
 */
public class MySQL5SurveyControllerDAO extends SurveyControllerDAO {

    private static final Logger log = LoggerFactory.getLogger(MySQL5SurveyControllerDAO.class);
    public static final String UPDATE_QUERY = "UPDATE `surveys` SET `used`=?, used_time=NOW() WHERE `unique_id`=? AND used=0";
    public static final String SELECT_QUERY = "SELECT * FROM `surveys` WHERE `used`=?";

    @Override
    public boolean supports(String arg0, int arg1, int arg2) {
        return MySQL5DAOUtils.supports(arg0, arg1, arg2);
    }

    @Override
    public FastList<SurveyItem> getAllNew() {
        FastList<SurveyItem> list = FastList.newInstance();

        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);
            stmt.setInt(1, 0);

            ResultSet rset = stmt.executeQuery();
            while (rset.next()) {
                SurveyItem item = new SurveyItem();
                item.uniqueId = rset.getInt("unique_id");
                item.ownerId = rset.getInt("owner_id");
                item.itemId = rset.getInt("item_id");
                item.count = rset.getLong("item_count");
                item.html = rset.getString("html_text");
                item.radio = rset.getString("html_radio");
                list.add(item);
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            log.warn("getAllNew() from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }

        return list;
    }

    @Override
    public boolean useItem(int id) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt;
            stmt = con.prepareStatement(UPDATE_QUERY);
            stmt.setInt(1, 1);
            stmt.setInt(2, id);
            stmt.execute();
            return stmt.getUpdateCount() >= 1;
        } catch (Exception e) {
            log.error("useItem", e);
            return false;
        } finally {
            DatabaseFactory.close(con);
        }
    }
}
