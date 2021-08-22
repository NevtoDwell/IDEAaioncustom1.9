/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.craft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.AdvCustomConfig;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.StaticObject;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RewardType;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.recipe.Component;
import com.ne.gs.model.templates.recipe.RecipeTemplate;
import com.ne.gs.network.aion.serverpackets.SM_CRAFT_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_CRAFT_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.item.ItemService.ItemAddPredicate;
import com.ne.gs.skillengine.task.CraftingTask;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * @author MrPoke, sphinx, synchro2
 */
public final class CraftService {

    private static final Logger log = LoggerFactory.getLogger("CRAFT_LOG");

    public static void finishCrafting(final Player player, RecipeTemplate recipetemplate, int critCount, int bonus) {
        if (recipetemplate.getMaxProductionCount() != null) {
            player.getRecipeList().deleteRecipe(player, recipetemplate.getId());
            if (critCount == 0) {
                QuestEngine.getInstance().onFailCraft(new QuestEnv(null, player, 0, 0),
                    recipetemplate.getComboProduct(1) == null ? 0 : recipetemplate.getComboProduct(1));
            }
        }

        int xpReward = (int) ((0.008 * (recipetemplate.getSkillpoint() + 100) * (recipetemplate.getSkillpoint() + 100) + 60));
        xpReward += xpReward * bonus / 100;
        int productItemId = critCount > 0 ? recipetemplate.getComboProduct(critCount) : recipetemplate.getProductid();

        ItemService.addItem(player, productItemId, recipetemplate.getQuantity(), new ItemAddPredicate() {

            @Override
            public boolean apply(Item item) {
                if (item.getItemTemplate().isWeapon() || item.getItemTemplate().isArmor()) {
                    item.setItemCreator(player.getName());
                }
                return true;
            }
        });
        ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(productItemId);
        if (LoggingConfig.LOG_CRAFT) {
            log.info(String.format("%s%s to player %s",
                critCount > 0
                    ? "[CRAFT][Critical] ID/Count"
                    : "[CRAFT][Normal] ID/Count",
                LoggingConfig.ENABLE_ADVANCED_LOGGING
                    ? String.format("/Item Name - %d/%d/%s", productItemId, recipetemplate.getQuantity(), itemTemplate.getName())
                    : String.format(" - %d/%d", productItemId, recipetemplate.getQuantity()), player.getName()));
        }

        if (player.getSkillList().addSkillXp(player, recipetemplate.getSkillid(), (int) RewardType.CRAFTING.calcReward(player, xpReward),
            recipetemplate.getSkillpoint())) {
            player.getCommonData().addExp(xpReward, RewardType.CRAFTING);
        } else {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DONT_GET_PRODUCTION_EXP(DescId.of(DataManager.SKILL_DATA.getSkillTemplate(
                recipetemplate.getSkillid()).getNameId())));
        }

        if (recipetemplate.getCraftDelayId() != null) {
            player.getCraftCooldownList().addCraftCooldown(recipetemplate.getCraftDelayId(),
                recipetemplate.getCraftDelayTime() / AdvCustomConfig.CRAFT_DELAYTIME_RATE);
        }
        player.getController().updateNearbyQuests();
    }

    public static void startCrafting(Player player, int recipeId, int targetObjId, int craftType) {
        RecipeTemplate recipeTemplate = DataManager.RECIPE_DATA.getRecipeTemplateById(recipeId);
        int skillId = recipeTemplate.getSkillid();
        VisibleObject target = player.getKnownList().getObject(targetObjId);
        ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getProductid());

        if (!checkCraft(player, recipeTemplate, skillId, target, itemTemplate, craftType)) {
            sendCancelCraft(player, skillId, targetObjId, itemTemplate);
            return;
        }

	    for(Component component : recipeTemplate.getComponent()) {
			player.getInventory().decreaseByItemId(component.getItemid(), component.getQuantity());
	    }

		if (recipeTemplate.getDp() != null) {
            player.getLifeStats().reduceDp(recipeTemplate.getDp());
        }

        int skillLvlDiff = player.getSkillList().getSkillLevel(skillId) - recipeTemplate.getSkillpoint();
        player.setCraftingTask(new CraftingTask(player, (StaticObject) target, recipeTemplate, skillLvlDiff, craftType == 1 ? 15 : 0));
        if (skillId == 40009) {
            player.getCraftingTask().setInterval(200);
        }
        player.getCraftingTask().start();
    }

    private static boolean checkCraft(Player player, RecipeTemplate recipeTemplate, int skillId, VisibleObject target,
                                      ItemTemplate itemTemplate, int craftType) {
        if (recipeTemplate == null) {
            return false;
        }

        if (itemTemplate == null) {
            return false;
        }

        if (player.getCraftingTask() != null && player.getCraftingTask().isInProgress()) {
            return false;
        }

        // morphing dont need static object/npc to use
        if ((skillId != 40009) && (target == null || !(target instanceof StaticObject))) {
            AuditLogger.info(player, " tried to craft incorrect target.");
            return false;
        }

        if (recipeTemplate.getDp() != null && (player.getLifeStats().getCurrentDp() < recipeTemplate.getDp())) {
            AuditLogger.info(player, " try craft without required DP count.");
            return false;
        }

        if (player.getInventory().isFull()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_COMBINE_INVENTORY_IS_FULL);
            return false;
        }

        if (recipeTemplate.getCraftDelayId() != null) {
            if (!player.getCraftCooldownList().isCanCraft(recipeTemplate.getCraftDelayId())) {
                AuditLogger.info(player, " try craft item before cooldown expire.");
                return false;
            }
        }

        for (Component component : recipeTemplate.getComponent()) {
            if (player.getInventory().getItemCountByItemId(component.getItemid()) < component.getQuantity()) {
                AuditLogger.info(player, " tried craft without required items.");
                return false;
            }
        }

        if ((craftType == 1) && (!player.getInventory().decreaseByItemId(getBonusReqItem(skillId), 1L))) {
            AuditLogger.info(player, " tried craft without 169401079.");
            return false;
        }
        if ((!player.getSkillList().isSkillPresent(skillId)) || (player.getSkillList().getSkillLevel(skillId) < recipeTemplate.getSkillpoint())) {
            AuditLogger.info(player, " tried craft without required skill.");
            return false;
        }

        return true;
    }

    private static void sendCancelCraft(Player player, int skillId, int targetObjId, ItemTemplate itemTemplate) {

        player.sendPck(new SM_CRAFT_UPDATE(skillId, itemTemplate, 0, 0, 4));
        PacketSendUtility.broadcastPacket(player, new SM_CRAFT_ANIMATION(player.getObjectId(), targetObjId, 0, 2), true);
    }

    private static int getBonusReqItem(int skillId) {
        switch (skillId) {
            case 40001:
                return 169401081;
            case 40002:
                return 169401076;
            case 40003:
                return 169401077;
            case 40004:
                return 169401078;
            case 40007:
                return 169401080;
            case 40008:
                return 169401079;
            case 40010:
                return 169401082;
            case 40005:
            case 40006:
            case 40009:
        }
        return 0;
    }
}
