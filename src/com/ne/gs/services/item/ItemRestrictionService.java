/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.item;

import com.ne.gs.configs.main.LegionConfig;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.team.legion.LegionPermissionsMask;
import com.ne.gs.model.templates.item.ItemCategory;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.LegionService;

/**
 * @author ATracer
 */
public final class ItemRestrictionService {

    /**
     * Check if item can be moved from storage by player
     */
    public static boolean isItemRestrictedFrom(Player player, byte storage) {
        StorageType type = StorageType.getStorageTypeById(storage);
        switch (type) {
            case LEGION_WAREHOUSE:
                if (!LegionService.getInstance().getLegionMember(player.getObjectId()).hasRights(LegionPermissionsMask.WH_WITHDRAWAL)
                    || !LegionConfig.LEGION_WAREHOUSE || !player.isLegionMember()) {
                    // You do not have the authority to use the Legion warehouse.
                    player.sendPck(new SM_SYSTEM_MESSAGE(1300322));
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Check if item can be moved to storage by player
     */
    public static boolean isItemRestrictedTo(Player player, Item item, byte storage) {
        StorageType type = StorageType.getStorageTypeById(storage);
        switch (type) {
            case REGULAR_WAREHOUSE:
                if (!item.isStorableinWarehouse(player)) {
                    // You cannot store this in the warehouse.
                    player.sendPck(new SM_SYSTEM_MESSAGE(1300418));
                    return true;
                }
                break;
            case ACCOUNT_WAREHOUSE:
                if (!item.isStorableinAccWarehouse(player)) {
                    // You cannot store this item in the account warehouse.
                    player.sendPck(new SM_SYSTEM_MESSAGE(1400356));
                    return true;
                }
                break;
            case LEGION_WAREHOUSE:
                if (!item.isStorableinLegWarehouse(player) || !LegionConfig.LEGION_WAREHOUSE) {
                    // You cannot store this item in the Legion warehouse.
                    player.sendPck(new SM_SYSTEM_MESSAGE(1400355));
                    return true;
                } else if (!player.isLegionMember()
                    || !LegionService.getInstance().getLegionMember(player.getObjectId()).hasRights(LegionPermissionsMask.WH_DEPOSIT)) {
                    // You do not have the authority to use the Legion warehouse.
                    player.sendPck(new SM_SYSTEM_MESSAGE(1300322));
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * Check, whether the item can be removed
     */
    public static boolean canRemoveItem(Player player, Item item) {
        ItemTemplate it = item.getItemTemplate();
        if (it.getCategory() == ItemCategory.QUEST) {
            // TODO: not removable, if quest status start and quest can not be abandoned
            // Waiting for quest data reparse
            return true;
        }
        return true;
    }

}
