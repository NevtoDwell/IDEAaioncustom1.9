/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import java.util.HashSet;
import java.util.Set;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.PlayerRecipesDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.templates.recipe.RecipeTemplate;
import com.ne.gs.network.aion.serverpackets.SM_LEARN_RECIPE;
import com.ne.gs.network.aion.serverpackets.SM_RECIPE_DELETE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author MrPoke
 */
public class RecipeList {

    private Set<Integer> recipeList = new HashSet<>();

    public RecipeList(HashSet<Integer> recipeList) {
        this.recipeList = recipeList;
    }

    public RecipeList() {
    }

    public Set<Integer> getRecipeList() {
        return recipeList;
    }

    public void addRecipe(Player player, RecipeTemplate recipeTemplate) {
        int recipeId = recipeTemplate.getId();
        if (!player.getRecipeList().isRecipePresent(recipeId)) {
            if (GDB.get(PlayerRecipesDAO.class).addRecipe(player.getObjectId(), recipeId)) {
                recipeList.add(recipeId);
                player.sendPck(new SM_LEARN_RECIPE(recipeId));
                player.sendPck(SM_SYSTEM_MESSAGE.STR_CRAFT_RECIPE_LEARN(recipeId, player.getName()));
            }
        }
    }

    public void addRecipe(int playerId, int recipeId) {
        if (GDB.get(PlayerRecipesDAO.class).addRecipe(playerId, recipeId)) {
            recipeList.add(recipeId);
        }
    }

    public void deleteRecipe(Player player, int recipeId) {
        if (recipeList.contains(recipeId)) {
            if (GDB.get(PlayerRecipesDAO.class).delRecipe(player.getObjectId(), recipeId)) {
                recipeList.remove(recipeId);
                player.sendPck(new SM_RECIPE_DELETE(recipeId));
            }
        }
    }

    public void autoLearnRecipe(Player player, int skillId, int skillLvl) {
        for (RecipeTemplate recipe : DataManager.RECIPE_DATA.getAutolearnRecipes(player.getRace(), skillId, skillLvl)) {
            player.getRecipeList().addRecipe(player, recipe);
        }
    }

    public boolean isRecipePresent(int recipeId) {
        return recipeList.contains(recipeId);
    }

    public int size() {
        return recipeList.size();
    }
}
