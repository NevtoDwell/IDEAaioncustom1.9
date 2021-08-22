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
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.database.dao.PlayerRecipesDAO;
import com.ne.gs.model.gameobjects.player.RecipeList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * @author lord_rex
 */
public class MySQL5PlayerRecipesDAO extends PlayerRecipesDAO {

    private static final String SELECT_QUERY = "SELECT `recipe_id` FROM player_recipes WHERE `player_id`=?";
    private static final String ADD_QUERY = "INSERT INTO player_recipes (`player_id`, `recipe_id`) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM player_recipes WHERE `player_id`=? AND `recipe_id`=?";

    @Override
    public RecipeList load(final int playerId) {
        final HashSet<Integer> recipeList = new HashSet<>();
        DB.select(SELECT_QUERY, new ParamReadStH() {

            @Override
            public void setParams(PreparedStatement ps) throws SQLException {
                ps.setInt(1, playerId);
            }

            @Override
            public void handleRead(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    recipeList.add(rs.getInt("recipe_id"));
                }
            }
        });
        return new RecipeList(recipeList);
    }

    @Override
    public boolean addRecipe(final int playerId, final int recipeId) {
        return DB.insertUpdate(ADD_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, playerId);
                ps.setInt(2, recipeId);
                ps.execute();
            }
        });
    }

    @Override
    public boolean delRecipe(final int playerId, final int recipeId) {
        return DB.insertUpdate(DELETE_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, playerId);
                ps.setInt(2, recipeId);
                ps.execute();
            }
        });
    }

    @Override
    public boolean supports(String s, int i, int i1) {
        return MySQL5DAOUtils.supports(s, i, i1);
    }
}
