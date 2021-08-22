/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.broker.filter;

import org.apache.commons.lang3.ArrayUtils;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.item.actions.CraftLearnAction;
import com.ne.gs.model.templates.item.actions.ItemActions;
import com.ne.gs.model.templates.recipe.RecipeTemplate;

public class BrokerRecipeFilter extends BrokerFilter {

    private final int craftSkillId;
    private final int[] masks;

    public BrokerRecipeFilter(int craftSkillId, int... masks) {
        this.craftSkillId = craftSkillId;
        this.masks = masks;
    }

    @Override
    public boolean accept(ItemTemplate template) {
        ItemActions actions = template.getActions();
        if (actions != null) {
            CraftLearnAction craftAction = actions.getCraftLearnAction();
            if (craftAction != null) {
                int id = craftAction.getRecipeId();
                RecipeTemplate recipeTemplate = DataManager.RECIPE_DATA.getRecipeTemplateById(id);
                if (recipeTemplate != null && recipeTemplate.getSkillid() == craftSkillId) {
                    return ArrayUtils.contains(masks, template.getTemplateId() / 100000);
                }
            }
        }
        return false;
    }
}
