/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.EnchantsConfig;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.ItemSlot;
import com.ne.gs.model.items.ManaStone;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.model.stats.calc.functions.StatEnchantFunction;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.model.stats.listeners.ItemEquipmentListener;
import com.ne.gs.model.templates.item.ArmorType;
import com.ne.gs.model.templates.item.ItemQuality;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.item.actions.EnchantItemAction;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemPacketService;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.item.ItemSocketService;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * @author ATracer
 * @modified Wakizashi, Source, vlog
 */
public final class EnchantService {

    private static final Logger log = LoggerFactory.getLogger(EnchantService.class);

    /**
     * @param player
     * @param targetItem
     * @param parentItem
     */
    public static boolean breakItem(Player player, Item targetItem, Item parentItem) {
        Storage inventory = player.getInventory();

        if (inventory.getItemByObjId(targetItem.getObjectId()) == null) {
            return false;
        }
        if (inventory.getItemByObjId(parentItem.getObjectId()) == null) {
            return false;
        }

        ItemTemplate itemTemplate = targetItem.getItemTemplate();
        int quality = itemTemplate.getItemQuality().getQualityId();

        if (!itemTemplate.isArmor() && !itemTemplate.isWeapon()) {
            AuditLogger.info(player, "Player try break dont compatible item type.");
            return false;
        }

        if (!itemTemplate.isArmor() && !itemTemplate.isWeapon()) {
            AuditLogger.info(player, "Break item hack, armor/weapon iD changed.");
            return false;
        }

        // Quality modifier
        if (itemTemplate.isSoulBound() && !itemTemplate.isArmor()) {
            quality += 1;
        } else if (!itemTemplate.isSoulBound() && itemTemplate.isArmor()) {
            quality -= 1;
        }

        int number = 0;
        int level = 1;
        switch (quality) {
            case 0: // JUNK
            case 1: // COMMON
                number = Rnd.get(1, 2);
                level = Rnd.get(-4, 8);
                break;
            case 2: // RARE
                number = Rnd.get(1, 4);
                level = Rnd.get(-4, 15);
                break;
            case 3: // LEGEND
                number = Rnd.get(1, 6);
                level = Rnd.get(-4, 25);
                break;
            case 4: // UNIQUE
                number = Rnd.get(1, 8);
                level = Rnd.get(35, 40);
                break;
            case 5: // EPIC
                number = Rnd.get(1, 6);
                level = Rnd.get(10, 45);
                break;
            case 6: // MYTHIC
            case 7:
                number = Rnd.get(1, 6);
                level = Rnd.get(35, 50);
                break;
        }

        // You can't add stone < 166000000
        if (level < 1) {
            level = 1;
        }
        int enchantItemLevel = targetItem.getItemTemplate().getLevel() + level;
        int enchantItemId = 166000000 + enchantItemLevel;

        if (inventory.delete(targetItem) != null) {
            if (inventory.decreaseByObjectId(parentItem.getObjectId(), 1)) {
                ItemService.addItem(player, enchantItemId, number);
            }
        } else {
            AuditLogger.info(player, "Possible break item hack, do not remove item.");
        }
        return true;
    }

    /**
     * @param player
     * @param parentItem
     *     the enchantment stone
     * @param targetItem
     *     the item to enchant
     * @param supplementItem
     *     the item, giving additional chance
     *
     * @return true, if successful
     */
    public static boolean enchantItem(Player player, Item parentItem, Item targetItem, Item supplementItem) {
        ItemTemplate enchantStone = parentItem.getItemTemplate();
        int enchantStoneLevel = enchantStone.getLevel();
        int targetItemLevel = targetItem.getItemTemplate().getLevel();
        int enchantitemLevel = targetItem.getEnchantLevel() + 1;

        // Modifier, depending on the quality of the item
        // Decreases the chance of enchant
        int qualityCap = 0;

        ItemQuality quality = targetItem.getItemTemplate().getItemQuality();

        switch (quality) {
            case JUNK:
            case COMMON:
                qualityCap = 5;
                break;
            case RARE:
                qualityCap = 10;
                break;
            case LEGEND:
                qualityCap = 15;
                break;
            case UNIQUE:
                qualityCap = 20;
                break;
            case EPIC:
                qualityCap = 25;
                break;
            case MYTHIC:
                qualityCap = 30;
                break;
        }

        // Start value of success
        float success = EnchantsConfig.ENCHANT_STONE;

        // Extra success chance
        // The greater the enchant stone level, the greater the
        // level difference modifier
        int levelDiff = enchantStoneLevel - targetItemLevel;
        success += levelDiff > 0 ? levelDiff * 3f / qualityCap : 0;

        // Level difference
        // Can be negative, if the item quality too hight
        // And the level difference too small
        success += levelDiff - qualityCap;

        // Enchant next level difficulty
        // The greater item enchant level,
        // the lower start success chance
        success -= targetItem.getEnchantLevel() * 2; // qualityCap / (enchantitemLevel > 10 ? 4f : 5f);

        // Supplement is used
        if (supplementItem != null) {
            // Amount of supplement items
            int supplementUseCount = 1;

            ItemTemplate supplementTemplate = supplementItem.getItemTemplate();

            float addSuccessRate = 0.0F;

            EnchantItemAction action = supplementTemplate.getActions().getEnchantAction();
            if (action != null) {
                if (action.isManastoneOnly()) {
                    return false;
                }
                addSuccessRate = action.getChance() * 2;
            }

            action = enchantStone.getActions().getEnchantAction();
            if (action != null) {
                supplementUseCount = action.getCount();
            }

            if (enchantitemLevel > 10) {
                supplementUseCount *= 2;
            }

            if (player.getInventory().getItemCountByItemId(supplementTemplate.getTemplateId()) < supplementUseCount) {
                return false;
            }

            switch (parentItem.getItemTemplate().getItemQuality()) {
                case LEGEND:
                    addSuccessRate *= EnchantsConfig.LESSER_SUP;
                    break;
                case UNIQUE:
                    addSuccessRate *= EnchantsConfig.REGULAR_SUP;
                    break;
                case EPIC:
                    addSuccessRate *= EnchantsConfig.GREATER_SUP;
            }

            success += addSuccessRate;

            player.subtractSupplements(supplementUseCount, supplementTemplate.getTemplateId());
        }

        // The overall success chance can't be more, than 95
        if (success >= 95) {
            success = 95;
        }

        boolean result = false;
        float random = Rnd.get(1, 1000) / 10f;

        // If the random number < overall success rate,
        // The item will be successfully enchanted
        if (random <= success) {
            result = true;
        }

        // For test purpose. To use by administrator
        if (player.getAccessLevel() > 2) {
            String msg = (result ? "Success" : "Fail") + " Rnd:" + random + " Luck:" + success;
            player.sendMsg(msg);
        }

        return result;
    }

    public static void enchantItemAct(Player player, Item parentItem, Item targetItem, Item supplementItem, int currentEnchant,
                                      boolean result) {
        ItemQuality targetQuality = targetItem.getItemTemplate().getItemQuality();
        if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
            AuditLogger.info(player, "Possible enchant hack, do not remove enchant stone.");
            return;
        }
        // Items that are Fabled or Eternal can get up to +15.
        player.updateSupplements();
        if (result) {
            switch (targetQuality) {
                case COMMON:
                case RARE:
                case LEGEND:
                    if (currentEnchant >= EnchantsConfig.ENCHANT_MAX_LEVEL_TYPE1) {
                        AuditLogger.info(player, "Possible enchant hack, send fake packet for enchant up more posible.");
                        return;
                    } else {
                        currentEnchant += 1;
                    }
                    break;
                case UNIQUE:
                case EPIC:
                case MYTHIC:
                    if (currentEnchant >= EnchantsConfig.ENCHANT_MAX_LEVEL_TYPE2) {
                        AuditLogger.info(player, "Possible enchant hack, send fake packet for enchant up more posible.");
                        return;
                    } else {
                        currentEnchant += 1;
                    }
                    break;
                case JUNK:
                    return;
            }
        } else // Retail: http://powerwiki.na.aiononline.com/aion/Patch+Notes:+1.9.0.1
            // When socketing fails at +11~+15, the value falls back to +10.
            if (currentEnchant > 10) {
                currentEnchant = 10;
            } else if (currentEnchant > 0) {
                currentEnchant -= 1;
            }

        targetItem.setEnchantLevel(currentEnchant);
        if (targetItem.isEquipped()) {
            player.getGameStats().updateStatsVisually();
        }

        ItemPacketService.updateItemAfterInfoChange(player, targetItem);

        if (targetItem.isEquipped()) {
            player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
        } else {
            player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
        }

        if (result) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_SUCCEED(DescId.of(targetItem.getNameID())));
        } else {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_FAILED(DescId.of(targetItem.getNameID())));
        }
    }

    /**
     * @param player
     * @param parentItem
     *     the manastone
     * @param targetItem
     *     the item to socket
     * @param supplementItem
     * @param targetWeapon
     *     fusioned weapon
     */
    public static boolean socketManastone(Player player, Item parentItem, Item targetItem, Item supplementItem, int targetWeapon) {

        int targetItemLevel = 1;

        // Fusioned weapon. Primary weapon level.
        if (targetWeapon == 1) {
            targetItemLevel = targetItem.getItemTemplate().getLevel();
            // Fusioned weapon. Secondary weapon level.
        } else {
            targetItemLevel = targetItem.getFusionedItemTemplate().getLevel();
        }

        int stoneLevel = parentItem.getItemTemplate().getLevel();
        int slotLevel = (int) (10 * Math.ceil((targetItemLevel + 10) / 10d));
        boolean result = false;

        // Start value of success
        float success = EnchantsConfig.MANA_STONE;

        // The current amount of socketed stones
        int stoneCount;

        // Manastone level shouldn't be greater as 20 + item level
        // Example: item level: 1 - 10. Manastone level should be <= 20
        if (stoneLevel > slotLevel) {
            return false;
        }

        // Fusioned weapon. Primary weapon slots.
        if (targetWeapon == 1) {
            // Count the inserted stones in the primary weapon
            stoneCount = targetItem.getItemStones().size();
            // Fusioned weapon. Secondary weapon slots.
        } else {
            // Count the inserted stones in the secondary weapon
            stoneCount = targetItem.getFusionStones().size();
        }

        // Fusioned weapon. Primary weapon slots.
        if (targetWeapon == 1) {
            // Find all free slots in the primary weapon
            if (stoneCount >= targetItem.getSockets(false)) {
                AuditLogger.info(player, "Manastone socket overload");
                return false;
            }
        }
        // Fusioned weapon. Secondary weapon slots.
        else if (!targetItem.hasFusionedItem() || stoneCount >= targetItem.getSockets(true)) {
            // Find all free slots in the secondary weapon
            AuditLogger.info(player, "Manastone socket overload");
            return false;
        }

        // Stone quality modifier
        success += parentItem.getItemTemplate().getItemQuality() == ItemQuality.COMMON ? 25f : 15f;

        // Next socket difficulty modifier
        float socketDiff = stoneCount * 1.25f + 1.75f;

        // Level difference
        success += (slotLevel - stoneLevel) / socketDiff;

        // The supplement item is used
        if (supplementItem != null) {
            int supplementUseCount = 0;
            ItemTemplate manastoneTemplate = parentItem.getItemTemplate();
            int manastoneCount = 0;
            // Not fusioned
            if (targetWeapon == 1) {
                manastoneCount = targetItem.getItemStones().size() + 1;
                // Fusioned
            } else {
                manastoneCount = targetItem.getFusionStones().size() + 1;
            }

            ItemTemplate supplementTemplate = supplementItem.getItemTemplate();
            float addSuccessRate = 0.0F;

            boolean isManastoneOnly = false;
            EnchantItemAction action = manastoneTemplate.getActions().getEnchantAction();
            if (action != null) {
                supplementUseCount = action.getCount();
            }
            action = supplementTemplate.getActions().getEnchantAction();
            if (action != null) {
                addSuccessRate = action.getChance();
                isManastoneOnly = action.isManastoneOnly();
            }

            switch (parentItem.getItemTemplate().getItemQuality()) {
                case LEGEND:
                    addSuccessRate *= EnchantsConfig.LESSER_SUP;
                    break;
                case UNIQUE:
                    addSuccessRate *= EnchantsConfig.REGULAR_SUP;
                    break;
                case EPIC:
                    addSuccessRate *= EnchantsConfig.GREATER_SUP;
            }

            if (isManastoneOnly) {
                supplementUseCount = 1;
            } else if (stoneCount > 0) {
                supplementUseCount = supplementUseCount * manastoneCount;
            }

            if (player.getInventory().getItemCountByItemId(supplementTemplate.getTemplateId()) < supplementUseCount) {
                return false;
            }

            success += addSuccessRate;

            player.subtractSupplements(supplementUseCount, supplementTemplate.getTemplateId());
        }

        float random = Rnd.get(1, 1000) / 10f;

        if (random <= success) {
            result = true;
        }

        // For test purpose. To use by administrator
        if (player.getAccessLevel() > 2) {
            String msg = (result ? "Success" : "Fail") + " Rnd:" + random + " Luck:" + success;
            player.sendMsg(msg);
        }

        return result;
    }

    public static void socketManastoneAct(Player player, Item parentItem, Item targetItem, Item supplementItem, int targetWeapon,
                                          boolean result) {
        player.updateSupplements();
        if (player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1) && result) {

            player.sendPck(SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_SUCCEED(DescId.of(targetItem.getNameID())));

            if (targetWeapon == 1) {
                ManaStone manaStone = ItemSocketService.addManaStone(targetItem, parentItem.getItemTemplate().getTemplateId());
                if (targetItem.isEquipped()) {
                    ItemEquipmentListener.addStoneStats(targetItem, manaStone, player.getGameStats());
                    player.getGameStats().updateStatsAndSpeedVisually();
                }
            } else {
                ManaStone manaStone = ItemSocketService.addFusionStone(targetItem, parentItem.getItemTemplate().getTemplateId());
                if (targetItem.isEquipped()) {
                    ItemEquipmentListener.addStoneStats(targetItem, manaStone, player.getGameStats());
                    player.getGameStats().updateStatsAndSpeedVisually();
                }
            }
        } else {

            player.sendPck(SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_FAILED(DescId.of(targetItem.getNameID())));
            if (targetWeapon == 1) {
                Set<ManaStone> manaStones = targetItem.getItemStones();
                if (targetItem.isEquipped()) {
                    ItemEquipmentListener.removeStoneStats(manaStones, player.getGameStats());
                    player.getGameStats().updateStatsAndSpeedVisually();
                }
                ItemSocketService.removeAllManastone(player, targetItem);
            } else {
                Set<ManaStone> manaStones = targetItem.getFusionStones();

                if (targetItem.isEquipped()) {
                    ItemEquipmentListener.removeStoneStats(manaStones, player.getGameStats());
                    player.getGameStats().updateStatsAndSpeedVisually();
                }

                ItemSocketService.removeAllFusionStone(player, targetItem);
            }
        }

        ItemPacketService.updateItemAfterInfoChange(player, targetItem);
    }

    /**
     * @param player
     * @param item
     */
    public static void onItemEquip(Player player, Item item) {
        List<IStatFunction> modifiers = new ArrayList<>();
        try {
            if (item.getItemTemplate().isWeapon()) {
                switch (item.getItemTemplate().getWeaponType()) {
                    case BOOK_2H:
                    case ORB_2H:
                        modifiers.add(new StatEnchantFunction(item, StatEnum.BOOST_MAGICAL_SKILL));
                        modifiers.add(new StatEnchantFunction(item, StatEnum.MAGICAL_ATTACK));
                        break;
                    case MACE_1H:
                    case STAFF_2H:
                        modifiers.add(new StatEnchantFunction(item, StatEnum.BOOST_MAGICAL_SKILL));
                    case DAGGER_1H:
                    case BOW:
                    case POLEARM_2H:
                    case SWORD_1H:
                    case SWORD_2H:
                        if (item.getEquipmentSlot() == ItemSlot.MAIN_HAND.id()) {
                            modifiers.add(new StatEnchantFunction(item, StatEnum.MAIN_HAND_POWER));
                        } else {
                            modifiers.add(new StatEnchantFunction(item, StatEnum.OFF_HAND_POWER));
                        }
                }
            } else if (item.getItemTemplate().isArmor()) {
                if (item.getItemTemplate().getArmorType() == ArmorType.SHIELD) {
                    modifiers.add(new StatEnchantFunction(item, StatEnum.DAMAGE_REDUCE));
                    modifiers.add(new StatEnchantFunction(item, StatEnum.BLOCK));
                } else {
                    modifiers.add(new StatEnchantFunction(item, StatEnum.PHYSICAL_DEFENSE));
                    modifiers.add(new StatEnchantFunction(item, StatEnum.MAGICAL_DEFEND));
                    modifiers.add(new StatEnchantFunction(item, StatEnum.MAXHP));
                    modifiers.add(new StatEnchantFunction(item, StatEnum.PHYSICAL_CRITICAL_RESIST));
                }
            }
            if (!modifiers.isEmpty()) {
                player.getGameStats().addEffect(item, modifiers);
            }
        } catch (Exception ex) {
            log.error("Error on item equip.", ex);
        }
    }
}
