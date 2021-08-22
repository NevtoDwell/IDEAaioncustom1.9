/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.recipe.RecipeTemplate;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author KID
 */
public final class RecipeService {

    public static RecipeTemplate validateNewRecipe(Player player, int recipeId) {
        if (player.getRecipeList().size() >= 1600) {
            player.sendMsg("You are unable to have more than 1600 recipes at the same time.");
            return null;
        }

        RecipeTemplate template = DataManager.RECIPE_DATA.getRecipeTemplateById(recipeId);
        if (template == null) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_RECIPEITEM_CANT_USE_NO_RECIPE);
            return null;
        }

        if (template.getRace() != Race.PC_ALL && template.getRace() != player.getRace()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CRAFTRECIPE_RACE_CHECK);
            return null;
        }

        if (player.getRecipeList().isRecipePresent(recipeId)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CRAFT_RECIPE_LEARNED_ALREADY);
            return null;
        }

        if (!player.getSkillList().isSkillPresent(template.getSkillid())) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CRAFT_RECIPE_CANT_LEARN_SKILL(DataManager.SKILL_DATA.getSkillTemplate(template.getSkillid()).getNameId()));
            return null;
        }

        if (template.getSkillpoint() > player.getSkillList().getSkillLevel(template.getSkillid())) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CRAFT_RECIPE_CANT_LEARN_SKILLPOINT);
            return null;
        }

        return template;
    }

    public static boolean addRecipe(Player player, int recipeId, boolean useValidation) {
        RecipeTemplate template = null;
        if (useValidation) {
            template = validateNewRecipe(player, recipeId);
        } else {
            template = DataManager.RECIPE_DATA.getRecipeTemplateById(recipeId);
        }

        if (template == null) {
            return false;
        }

        player.getRecipeList().addRecipe(player, template);
        return true;
    }

}
