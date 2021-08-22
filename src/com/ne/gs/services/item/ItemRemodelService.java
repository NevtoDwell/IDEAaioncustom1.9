/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.item;

import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.templates.item.ArmorType;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.trade.PricesService;

public final class ItemRemodelService {

    public static void remodelItem(Player player, int keepItemObjId, int extractItemObjId) {
        Storage inventory = player.getInventory();
        Item keepItem = inventory.getItemByObjId(keepItemObjId);
        Item extractItem = inventory.getItemByObjId(extractItemObjId);

        long remodelCost = PricesService.getPriceForService(1000L, player.getRace());

        if ((keepItem == null) || (extractItem == null)) {
            return;
        }

        if (player.getLevel() < 10) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_PC_LEVEL_LIMIT);
            return;
        }

        if (player.getInventory().getKinah() < remodelCost) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NOT_ENOUGH_GOLD(DescId.of(keepItem.getItemTemplate().getNameId())));
            return;
        }

        if (extractItem.getItemTemplate().getTemplateId() == 168100000) {
            if (keepItem.getItemTemplate() == keepItem.getItemSkinTemplate()) {
                player.sendMsg("That item does not have a remodeled skin to remove.");
                return;
            }

            player.getInventory().decreaseKinah(remodelCost);

            player.getInventory().decreaseItemCount(extractItem, 1L);

            keepItem.setItemSkinTemplate(keepItem.getItemTemplate());

            if (!keepItem.getItemTemplate().isItemDyePermitted()) {
                keepItem.setItemColor(0);
            }

            ItemPacketService.updateItemAfterInfoChange(player, keepItem);
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_SUCCEED(DescId.of(keepItem.getItemTemplate().getNameId())));
            return;
        }

        if ((keepItem.getItemTemplate().getWeaponType() != extractItem.getItemSkinTemplate().getWeaponType())
            || ((extractItem.getItemSkinTemplate().getArmorType() != ArmorType.CLOTHES) && (keepItem.getItemTemplate().getArmorType() != extractItem
            .getItemSkinTemplate().getArmorType())) || (keepItem.getItemTemplate().getArmorType() == ArmorType.CLOTHES)
            || (keepItem.getItemTemplate().getItemSlot() != extractItem.getItemSkinTemplate().getItemSlot())) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NOT_COMPATIBLE(
                DescId.of(keepItem.getItemTemplate().getNameId()), DescId.of(extractItem.getItemSkinTemplate().getNameId())));
            return;
        }

        if (!keepItem.isRemodelable(player)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1300478, DescId.of(keepItem.getItemTemplate().getNameId())));

            return;
        }

        if (!extractItem.isRemodelable(player)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1300482, DescId.of(extractItem.getItemTemplate().getNameId())));

            return;
        }

        player.getInventory().decreaseKinah(remodelCost);

        player.getInventory().decreaseItemCount(extractItem, 1L);

        keepItem.setItemSkinTemplate(extractItem.getItemSkinTemplate());

        keepItem.setItemColor(extractItem.getItemColor());

        ItemPacketService.updateItemAfterInfoChange(player, keepItem);
        player.sendPck(new SM_SYSTEM_MESSAGE(1300483, DescId.of(keepItem.getItemTemplate().getNameId())));
    }
}
