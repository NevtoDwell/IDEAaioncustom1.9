/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.gameobjects.player.RecipeList;

/**
 * @author lord_rex
 */
public abstract class PlayerRecipesDAO implements DAO {

    @Override
    public String getClassName() {
        return PlayerRecipesDAO.class.getName();
    }

    public abstract RecipeList load(int playerId);

    public abstract boolean addRecipe(int playerId, int recipeId);

    public abstract boolean delRecipe(int playerId, int recipeId);
}
