/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.gs.database.GDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.dao.InventoryDAO;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemQuality;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemPacketService;
import com.ne.gs.services.item.ItemSocketService;
import com.ne.gs.services.trade.PricesService;

/**
 * This class is responsible of Armsfusion-related tasks (fusion,breaking)
 *
 * @author Wakizashi modified by Source & xTz
 */
public final class ArmsfusionService {

    private static final Logger log = LoggerFactory.getLogger(ArmsfusionService.class);

    public static void fusionWeapons(Player player, int firstItemUniqueId, int secondItemUniqueId) {
        Item firstItem = player.getInventory().getItemByObjId(firstItemUniqueId);
        if (firstItem == null) {
            firstItem = player.getEquipment().getEquippedItemByObjId(firstItemUniqueId);
        }

        Item secondItem = player.getInventory().getItemByObjId(secondItemUniqueId);
        if (secondItem == null) {
            secondItem = player.getEquipment().getEquippedItemByObjId(secondItemUniqueId);
        }

		/*
         * Check if item is in bag
		 */
        if (firstItem == null || secondItem == null || !(player.getTarget() instanceof Npc)) {
            return;
        }

        double priceRate = PricesService.getGlobalPrices(player.getRace()) * .01;
        double taxRate = PricesService.getTaxes(player.getRace()) * .01;
        double rarity = rarityRate(firstItem.getItemTemplate().getItemQuality());
        int priceMod = PricesService.getGlobalPricesModifier() * 2;
        int level = firstItem.getItemTemplate().getLevel();

        int price = (int) (priceMod * priceRate * taxRate * rarity * level * level);
        log.debug("Rarete: " + rarity + " Prix Ratio: " + priceRate + " Tax: " + taxRate + " Mod: " + priceMod + " NiveauDeLArme: " + level);
        log.debug("Prix: " + price);

        if (player.getInventory().getKinah() < price) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_ENOUGH_MONEY(firstItem.getNameID(), secondItem.getNameID()));
            return;
        }

		/*
		 * Fusioned weapons must be not fusioned
		 */
        if (firstItem.hasFusionedItem()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_AVAILABLE(firstItem.getNameID()));
            return;
        }
        if (secondItem.hasFusionedItem()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_AVAILABLE(secondItem.getNameID()));
            return;
        }

        if (!firstItem.getItemTemplate().isCanFuse() || !secondItem.getItemTemplate().isCanFuse()) {
            player.sendMsg("You performed illegal operation, admin will catch you");
            log.info("[AUDIT] Client hack with item fusion, player: " + player.getName());
            return;
        }

        if (!firstItem.getItemTemplate().isTwoHandWeapon()) {
            // TODO retail message
            player.sendPck(SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_AVAILABLE(firstItem.getNameID()));
            return;
        }

        // Fusioned weapons must have same type
        if (firstItem.getItemTemplate().getWeaponType() != secondItem.getItemTemplate().getWeaponType()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_DIFFERENT_TYPE);
            return;
        }

		/*
		 * Second weapon must have inferior or equal lvl. in relation to first weapon
		 */
        if (secondItem.getItemTemplate().getLevel() > firstItem.getItemTemplate().getLevel()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_MAIN_REQUIRE_HIGHER_LEVEL);
            return;
        }

        if (firstItem.getImprovement() != null && secondItem.getImprovement() != null
            && firstItem.getImprovement().getChargeWay() != secondItem.getImprovement().getChargeWay()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_COMPARABLE_ITEM);
            return;
        }
        firstItem.setFusionedItem(secondItem.getItemTemplate());

        ItemSocketService.removeAllFusionStone(player, firstItem);

        if (secondItem.hasOptionalSocket()) {
            firstItem.setOptionalFusionSocket(secondItem.getOptionalSocket());
        } else {
            firstItem.setOptionalFusionSocket(0);
        }

        ItemSocketService.copyFusionStones(secondItem, firstItem);

	    if(!firstItem.getExchangeTime().isExpired()) {
		    firstItem.getExchangeTime().expireNow();
	    }

        GDB.get(InventoryDAO.class).store(firstItem, player);

        if (!player.getInventory().decreaseByObjectId(secondItemUniqueId, 1)) {
            return;
        }

        ItemPacketService.updateItemAfterInfoChange(player, firstItem);
        player.getInventory().decreaseKinah(price);

        player.sendPck(SM_SYSTEM_MESSAGE.STR_COMPOUND_SUCCESS(firstItem.getNameID(), secondItem.getNameID()));
    }

    private static double rarityRate(ItemQuality rarity) {
        switch (rarity) {
            case COMMON:
                return 1.0;
            case RARE:
                return 1.25;
            case LEGEND:
                return 1.5;
            case UNIQUE:
                return 2.0;
            case EPIC:
                return 2.5;
            default:
                return 1.0;
        }
    }

    public static void breakWeapons(Player player, int weaponToBreakUniqueId) {
        Item weaponToBreak = player.getInventory().getItemByObjId(weaponToBreakUniqueId);
        if (weaponToBreak == null) {
            weaponToBreak = player.getEquipment().getEquippedItemByObjId(weaponToBreakUniqueId);
        }

        if (weaponToBreak == null || !(player.getTarget() instanceof Npc)) {
            return;
        }

        if (!weaponToBreak.hasFusionedItem()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_DECOMPOUND_ERROR_NOT_AVAILABLE(weaponToBreak.getNameID()));
            return;
        }

        weaponToBreak.setFusionedItem(null);

        ItemSocketService.removeAllFusionStone(player, weaponToBreak);
        GDB.get(InventoryDAO.class).store(weaponToBreak, player);

        ItemPacketService.sendAllItemsInfo(player);
        
        player.sendPck(SM_SYSTEM_MESSAGE.STR_COMPOUNDED_ITEM_DECOMPOUND_SUCCESS(weaponToBreak.getNameID()));
        
        
    }
}
