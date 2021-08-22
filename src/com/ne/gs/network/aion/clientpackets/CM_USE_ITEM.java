/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.actions.AbstractItemAction;
import com.ne.gs.model.templates.item.actions.ItemActions;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.HandlerResult;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.restrictions.RestrictionsManager;

/**
 * @author Avol
 */
public class CM_USE_ITEM extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_USE_ITEM.class);

    public int uniqueItemId;
    public int type, targetItemId;

    @Override
    protected void readImpl() {
        uniqueItemId = readD();
        type = readC();
        if (type == 2) {
            targetItemId = readD();
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (player.isProtectionActive()) {
            player.getController().stopProtectionActiveTask();
        }

        Item item = player.getInventory().getItemByObjId(uniqueItemId);
        Item targetItem = player.getInventory().getItemByObjId(targetItemId);

        if (item == null) {
            log.warn(String.format("CHECKPOINT: null item use action: %d %d", player.getObjectId(), uniqueItemId));
            return;
        }

        // TODO make proper validation
        if (targetItem == null) {
            targetItem = player.getEquipment().getEquippedItemByObjId(targetItemId);
        }

        //        if (targetItem == null) {
        //            log.warn(String.format("CHECKPOINT: targetItem == null %d %d", player.getObjectId(), targetItemId));
        //            return;
        //        }

        if (item.getItemTemplate().getTemplateId() == 165000001 && targetItem != null && targetItem.getItemTemplate().canExtract()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR);
            return;
        }

        // check use item multicast delay exploit cast (spam)
        if (player.isCasting()) {
            // PacketSendUtility.sendMessage(this.getOwner(),
            // "You must wait until cast time finished to use skill again.");
            player.getController().cancelCurrentSkill();
            // On retail the item is cancelling the current skill and then procs normally
            // return;
        }

        if (!RestrictionsManager.canUseItem(player, item)) {
            return;
        }

        if (item.getItemTemplate().getRace() != Race.PC_ALL && item.getItemTemplate().getRace() != player.getRace()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_RACE);
            return;
        }

        int requiredLevel = item.getItemTemplate().getRequiredLevel(player.getCommonData().getPlayerClass());
        if (requiredLevel == -1) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_CLASS);
            return;
        }

        if (requiredLevel > player.getLevel()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL(item.getNameID(), requiredLevel));
            return;
        }

        HandlerResult result = QuestEngine.getInstance().onItemUseEvent(new QuestEnv(null, player, 0, 0), item);
        if (result == HandlerResult.FAILED) {
            return; // don't remove item
        }

        ItemActions itemActions = item.getItemTemplate().getActions();
        ArrayList<AbstractItemAction> actions = new ArrayList<>();

        if (itemActions == null) {
            return;
        }

        for (AbstractItemAction itemAction : itemActions.getItemActions()) {
            // check if the item can be used before placing it on the cooldown list.
            if (itemAction.canAct(player, item, targetItem)) {
                actions.add(itemAction);
            }
        }

        if (actions.size() == 0) {
            return;
        }

        // Store Item CD in server Player variable.
        // Prevents potion spamming, and relogging to use kisks/aether jelly/long CD items.
        if (player.isItemUseDisabled(item.getItemTemplate().getUseLimits())) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_CANT_USE_UNTIL_DELAY_TIME);
            return;
        }

        int useDelay = player.getItemCooldown(item.getItemTemplate());
        if (useDelay > 0) {
            player.addItemCoolDown(item.getItemTemplate().getUseLimits().getDelayId(), System.currentTimeMillis() + useDelay, useDelay / 1000);
        }

        // notify item use observer
        player.getObserveController().notifyItemuseObservers(item);

        for (AbstractItemAction itemAction : actions) {
            itemAction.act(player, item, targetItem);
        }
    }
}
