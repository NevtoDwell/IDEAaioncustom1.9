/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.task;

import com.ne.commons.utils.Rnd;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.StaticObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.recipe.RecipeTemplate;
import com.ne.gs.modules.housing.HouseInfo;
import com.ne.gs.network.aion.serverpackets.SM_CRAFT_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_CRAFT_UPDATE;
import com.ne.gs.services.craft.CraftService;
import com.ne.gs.utils.PacketSendUtility;

import static com.ne.gs.modules.housing.House.HouseType.ESTATE;
import static com.ne.gs.modules.housing.House.HouseType.MANSION;

/**
 * @author Mr. Poke, synchro2
 */
public class CraftingTask extends AbstractCraftTask {

    protected RecipeTemplate recipeTemplate;
    protected ItemTemplate itemTemplate;
    protected int critCount;
    protected boolean crit = false;
    protected int maxCritCount;
    private final int bonus;

    /**
     * @param requestor
     * @param responder
     */

    public CraftingTask(Player requestor, StaticObject responder, RecipeTemplate recipeTemplate, int skillLvlDiff, int bonus) {
        super(requestor, responder, skillLvlDiff);
        this.recipeTemplate = recipeTemplate;
        this.maxCritCount = recipeTemplate.getComboProductSize();
        this.bonus = bonus;
    }

    /*
     * (non-Javadoc)
     * @see com.ne.gs.skillengine.task.AbstractCraftTask#onFailureFinish()
     */
    @Override
    protected void onFailureFinish() {
        requestor.sendPck(new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue, currentFailureValue, 6));

        PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), 0, 3), true);
    }

    /*
     * (non-Javadoc)
     * @see com.ne.gs.skillengine.task.AbstractCraftTask#onSuccessFinish()
     */
    @Override
    protected boolean onSuccessFinish() {
        if (crit && recipeTemplate.getComboProduct(critCount) != null) {

            requestor.sendPck(new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 3));
            onInteractionStart();
            return false;
        } else {
            requestor.sendPck(new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue, currentFailureValue, 5));

            PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), 0, 2), true);
            CraftService.finishCrafting(requestor, recipeTemplate, critCount, bonus);
            return true;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.ne.gs.skillengine.task.AbstractCraftTask#sendInteractionUpdate()
     */
    @Override
    protected void sendInteractionUpdate() {
        requestor.sendPck(new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, currentSuccessValue, currentFailureValue, 1));
    }

    /*
     * (non-Javadoc)
     * @see com.ne.gs.skillengine.task.AbstractInteractionTask#onInteractionAbort()
     */
    @Override
    protected void onInteractionAbort() {
        requestor.sendPck(new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 4));

        PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), 0, 2), true);
        requestor.setCraftingTask(null);
    }

    /*
     * (non-Javadoc)
     * @see com.ne.gs.skillengine.task.AbstractInteractionTask#onInteractionFinish()
     */
    @Override
    protected void onInteractionFinish() {
        requestor.setCraftingTask(null);
    }

    /*
     * (non-Javadoc)
     * @see com.ne.gs.skillengine.task.AbstractInteractionTask#onInteractionStart()
     */
    @Override
    protected void onInteractionStart() {
        currentSuccessValue = 0;
        currentFailureValue = 0;
        checkCrit();
        int chance = requestor.getRates().getCraftCritRate();
        if (maxCritCount > 0) {
            if ((critCount > 0) && (maxCritCount > 1)) {
                chance = requestor.getRates().getComboCritRate();
                if (HouseInfo.of(requestor).typeIs(ESTATE, MANSION)) {
                    chance += 5;
                }
            }
            if ((critCount < maxCritCount) && (Rnd.get(100) < chance)) {
                critCount++;
                crit = true;
            }
        }

        requestor.sendPck(new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, completeValue, completeValue, 0));
        requestor.sendPck(new SM_CRAFT_UPDATE(recipeTemplate.getSkillid(), itemTemplate, 0, 0, 1));

        PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), recipeTemplate.getSkillid(), 0), true);

        PacketSendUtility.broadcastPacket(requestor, new SM_CRAFT_ANIMATION(requestor.getObjectId(), responder.getObjectId(), recipeTemplate.getSkillid(), 1), true);
    }

    protected void checkCrit() {
        if (crit) {
            crit = false;
            this.itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getComboProduct(critCount));
        } else {
            this.itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getProductid());
        }
    }
}
