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
import java.util.Iterator;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemCategory;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.EnchantService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;

/**
 * @author Nemiroff, Wakizashi, vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnchantItemAction")
public class EnchantItemAction extends AbstractItemAction {

    @XmlAttribute(name = "count")
    private int count;

    @XmlAttribute(name = "min_level")
    private Integer min_level;

    @XmlAttribute(name = "max_level")
    private Integer max_level;

    @XmlAttribute(name = "manastone_only")
    private boolean manastone_only;

    @XmlAttribute(name = "chance")
    private float chance;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        if (isSupplementAction()) {
            return false;
        }
        if (targetItem == null) { // no item selected.
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR);
            return false;
        }
        if (parentItem == null) {
            return false;
        }
        int msID = parentItem.getItemTemplate().getTemplateId() / 1000000;
        int tID = targetItem.getItemTemplate().getTemplateId() / 1000000;
        if ((msID != 167 && msID != 166) || tID >= 120) {
            return false;
        }
        return true;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        act(player, parentItem, targetItem, null, 1);
    }

    // necessary overloading to not change AbstractItemAction

    public void act(final Player player, final Item parentItem, final Item targetItem, final Item supplementItem, final int targetWeapon) {
        if ((supplementItem != null) && (!checkSupplementLevel(player, supplementItem.getItemTemplate(), targetItem.getItemTemplate()))) {
            return;
        }
        final int currentEnchant = targetItem.getEnchantLevel();
        final boolean isSuccess = isSuccess(player, parentItem, targetItem, supplementItem, targetWeapon, currentEnchant);
        player.getController().cancelUseItem();

        PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 5000, 0, 0));
        player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                ItemTemplate itemTemplate = parentItem.getItemTemplate();
                // Enchantment stone
                if (itemTemplate.getCategory() == ItemCategory.ENCHANTMENT) {
                    EnchantService.enchantItemAct(player, parentItem, targetItem, supplementItem, currentEnchant, isSuccess);
                } else {
                    EnchantService.socketManastoneAct(player, parentItem, targetItem, supplementItem, targetWeapon, isSuccess);
                }
                PacketSendUtility
                    .broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, isSuccess ? 1
                        : 2, 0));
                if (CustomConfig.ENABLE_ENCHANT_ANNOUNCE) {
                    if (itemTemplate.getCategory() == ItemCategory.ENCHANTMENT && currentEnchant == 14 && isSuccess) {
                        Iterator<Player> iter = World.getInstance().getPlayersIterator();
                        while (iter.hasNext()) {
                            Player player2 = iter.next();
                            if (player2.getRace() == player.getRace()) {
                                player2.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_ENCHANT_ITEM_SUCCEEDED_15(player.getName(), targetItem.getItemTemplate().getNameId()));
                            }
                        }
                    }
                }
            }

        }, 5000));
    }

    /**
     * Check, if the item enchant will be successful
     *
     * @param player
     * @param parentItem
     *     the enchantment-/manastone to insert
     * @param targetItem
     *     the current item to enchant
     * @param supplementItem
     *     the item to increase the enchant chance (if exists)
     * @param targetWeapon
     *     the fused weapon (if exists)
     * @param currentEnchant
     *     current enchant level
     *
     * @return true if successful
     */
    private boolean isSuccess(Player player, Item parentItem, Item targetItem, Item supplementItem, int targetWeapon, int currentEnchant) {
        if (parentItem.getItemTemplate() != null) {
            // Id of the stone
            ItemTemplate itemTemplate = parentItem.getItemTemplate();
            // Enchantment stone
            if (itemTemplate.getCategory() == ItemCategory.ENCHANTMENT) {
                return EnchantService.enchantItem(player, parentItem, targetItem, supplementItem);
            }
            // Manastone
            return EnchantService.socketManastone(player, parentItem, targetItem, supplementItem, targetWeapon);
        }
        return false;
    }

    public int getCount() {
        return count;
    }

    public int getMaxLevel() {
        return max_level != null ? max_level : 0;
    }

    public int getMinLevel() {
        return min_level != null ? min_level : 0;
    }

    public boolean isManastoneOnly() {
        return manastone_only;
    }

    public float getChance() {
        return chance;
    }

    boolean isSupplementAction() {
        return getMinLevel() > 0 || getMaxLevel() > 0 || getChance() > 0.0f || isManastoneOnly();
    }

    private boolean checkSupplementLevel(Player player, ItemTemplate supplementTemplate, ItemTemplate targetItemTemplate) {
        if (supplementTemplate.getCategory() != ItemCategory.ENCHANTMENT) {
            int minEnchantLevel = targetItemTemplate.getLevel();
            int maxEnchantLevel = targetItemTemplate.getLevel();

            EnchantItemAction action = supplementTemplate.getActions().getEnchantAction();
            if (action != null) {
                if (action.getMinLevel() != 0) {
                    minEnchantLevel = action.getMinLevel();
                }
                if (action.getMaxLevel() != 0) {
                    maxEnchantLevel = action.getMaxLevel();
                }
            }
            if (minEnchantLevel <= targetItemTemplate.getLevel() && maxEnchantLevel >= targetItemTemplate.getLevel()) {
                return true;
            }
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_ENCHANT_ASSISTANT_NO_RIGHT_ITEM);
            return false;
        }
        return true;
    }
}
