/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.RecipeService;

/**
 * @author ATracer, MrPoke, KID
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CraftLearnAction")
public class CraftLearnAction extends AbstractItemAction {

    @XmlAttribute
    protected int recipeid;

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        player.getController().cancelUseItem();
        if (player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
            if (RecipeService.addRecipe(player, recipeid, false)) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_USE_ITEM(DescId.of(parentItem.getItemTemplate().getNameId())));
                player.sendPck(new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId()));
            }
        }
    }

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        return RecipeService.validateNewRecipe(player, recipeid) != null;
    }

    public int getRecipeId() {
        return recipeid;
    }
}
