/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.item;

import java.util.Collection;
import java.util.List;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.database.dao.ItemStoneListDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Equipment;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.ItemId;
import com.ne.gs.model.items.ManaStone;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.templates.item.ArmorType;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.quest.QuestItems;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;
import com.ne.gs.taskmanager.tasks.ExpireTimerTask;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;

/**
 * @author KID
 */
public final class ItemService {

    private static final Logger log = LoggerFactory.getLogger("ITEM_LOG");

    private static final ItemAddPredicate DEFAULT_ADD_PREDICATE = new ItemAddPredicate();

    public static void loadItemStones(Collection<Item> itemList) {
        if (itemList != null && itemList.size() > 0) {
            GDB.get(ItemStoneListDAO.class).load(itemList);
        }
    }

    public static long addItemQ(Player player, int itemId, long count) {
        return addItemQ(player, itemId, count, DEFAULT_ADD_PREDICATE);
    }

    public static long addItemQ(Player player, int itemId, long count, ItemAddPredicate predicate) {
        return addItemQ(player, itemId, count, null, predicate);
    }

    public static long addItemQ(Player player, int itemId, long count, Item sourceItem, ItemAddPredicate predicate) {
        ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
        if (count <= 0 || itemTemplate == null) {
            return 0;
        }
        Preconditions.checkNotNull(itemTemplate, "No item with id " + itemId);
        Preconditions.checkNotNull(predicate, "Predicate is not supplied");

        if (LoggingConfig.LOG_ITEM) {
            log.info("[ITEM] ID/Count" + (LoggingConfig.ENABLE_ADVANCED_LOGGING ? "/Item Name - " + itemTemplate.getTemplateId() + "/" + count + "/" + itemTemplate.getName() : " - " + itemTemplate.getTemplateId() + "/" + count)
                    + " to player " + player.getName());
        }

        Storage inventory = player.getInventory();
        if (itemTemplate.isKinah()) {
            inventory.increaseKinah(count);
            return 0;
        }

        if (itemTemplate.isStackable()) {
            count = addStackableItemQ(player, itemTemplate, count, predicate);
        } else {
            count = addNonStackableItemQ(player, itemTemplate, count, sourceItem, predicate);
        }

        if (inventory.isFullQ() && count > 0) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR);
        }
        return count;
    }

    private static long addNonStackableItemQ(Player player, ItemTemplate itemTemplate, long count, Item sourceItem,
            Predicate<Item> predicate) {
        Storage inventory = player.getInventory();
        while (!inventory.isFullQ() && count > 0) {
            Item newItem = ItemFactory.newItem(itemTemplate.getTemplateId());

            if (newItem.getExpireTime() != 0) {
                ExpireTimerTask.getInstance().addTask(newItem, player);
            }
            if (sourceItem != null) {
                copyItemInfo(sourceItem, newItem);
            }
            predicate.apply(newItem);
            inventory.add(newItem);
            count--;
        }
        return count;
    }

    private static long addStackableItemQ(Player player, ItemTemplate itemTemplate, long count, ItemAddPredicate predicate) {
        Storage inventory = player.getInventory();
        Collection<Item> items = inventory.getItemsByItemId(itemTemplate.getTemplateId());
        for (Item item : items) {
            if (count == 0) {
                break;
            }
            count = inventory.increaseItemCount(item, count, predicate.getUpdateType(item));
        }

        //dirty & hacky check for arrows and shards...
        if (itemTemplate.getArmorType() == ArmorType.SHARD || itemTemplate.getArmorType() == ArmorType.ARROW) {
            Equipment equipement = player.getEquipment();
            items = equipement.getEquippedItemsByItemId(itemTemplate.getTemplateId());
            for (Item item : items) {
                if (count == 0) {
                    break;
                }
                count = equipement.increaseEquippedItemCount(item, count);
            }
        }

        while (!inventory.isFullQ() && count > 0) {
            Item newItem = ItemFactory.newItem(itemTemplate.getTemplateId(), count);
            count -= newItem.getItemCount();
            inventory.add(newItem);
        }
        return count;
    }
    
    
    
    
    public static long addItem(Player player, int itemId, long count) {
        return addItem(player, itemId, count, DEFAULT_ADD_PREDICATE);
    }

    public static long addItem(Player player, int itemId, long count, boolean toOverflow) {
        return addItem(player, itemId, count, null, DEFAULT_ADD_PREDICATE, toOverflow);
    }


    public static long addItem(Player player, int itemId, long count, ItemAddPredicate predicate) {
        return addItem(player, itemId, count, null, predicate, false);
    }

    /**
     * Add new item based on all sourceItem values
     */
    public static long addItem(Player player, Item sourceItem) {
        return addItem(player, sourceItem.getItemId(), sourceItem.getItemCount(), sourceItem, DEFAULT_ADD_PREDICATE, false);
    }

    public static long addItem(Player player, Item sourceItem, ItemAddPredicate predicate) {
        return addItem(player, sourceItem.getItemId(), sourceItem.getItemCount(), sourceItem, predicate, false);
    }

    public static long addItem(Player player, int itemId, long count, Item sourceItem) {
        return addItem(player, itemId, count, sourceItem, DEFAULT_ADD_PREDICATE, false);
    }

    /**
     * Add new item based on sourceItem values
     */
    public static long addItem(Player player, int itemId, long count, Item sourceItem, ItemAddPredicate predicate, boolean toOverflow) {
        ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
        if (count <= 0 || itemTemplate == null) {
            return 0;
        }
        Preconditions.checkNotNull(itemTemplate, "No item with id " + itemId);
        Preconditions.checkNotNull(predicate, "Predicate is not supplied");

        if (LoggingConfig.LOG_ITEM) {
            log.info("[ITEM] ID/Count"
                    + (LoggingConfig.ENABLE_ADVANCED_LOGGING ? "/Item Name - " + itemTemplate.getTemplateId() + "/" + count + "/" + itemTemplate.getName()
                            : " - " + itemTemplate.getTemplateId() + "/" + count) + " to player " + player.getName());
        }

        Storage inventory = player.getInventory();
        if (itemTemplate.isKinah()) {
            inventory.increaseKinah(count);
            return 0;
        }

        if (itemTemplate.isStackable()) {
            count = addStackableItem(player, itemTemplate, count, predicate, toOverflow);
        } else {
            count = addNonStackableItem(player, itemTemplate, count, sourceItem, predicate, toOverflow);
        }
        if (inventory.isFull() && count > 0) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR);
        }
        return count;
    }

    /**
     * Add non-stackable item to inventory
     */
    private static long addNonStackableItem(Player player, ItemTemplate itemTemplate, long count, Item sourceItem,
            Predicate<Item> predicate, boolean toOverflow) {
        Storage inventory = player.getInventory();
        long c = count;
        while ((!inventory.isFull() || toOverflow) && count > 0) {
            Item newItem = ItemFactory.newItem(itemTemplate.getTemplateId());

            if (newItem.getExpireTime() != 0) {
                ExpireTimerTask.getInstance().addTask(newItem, player);
            }
            if (sourceItem != null) {
                copyItemInfo(sourceItem, newItem);
            }

            predicate.apply(newItem);
            inventory.add(newItem);
            count--;
        }

        long added = c - count;

        if(added > 0) {
            for (int i = 0; i < added; i++){
                player.sendPck(new SM_SYSTEM_MESSAGE(1390000, DescId.of(itemTemplate.getNameId()))); // You have aquired %s
            }
        }

        return count;
    }

    /**
     * Copy some item values like item stones and enchange level
     */
    private static void copyItemInfo(Item sourceItem, Item newItem) {
        newItem.setOptionalSocket(sourceItem.getOptionalSocket());
        if (sourceItem.hasManaStones()) {
            for (ManaStone manaStone : sourceItem.getItemStones()) {
                ItemSocketService.addManaStone(newItem, manaStone.getItemId());
            }
        }
        if (sourceItem.getGodStone() != null) {
            newItem.addGodStone(sourceItem.getGodStone().getItemId());
        }
        if (sourceItem.getEnchantLevel() > 0) {
            newItem.setEnchantLevel(sourceItem.getEnchantLevel());
        }
        if (sourceItem.isSoulBound()) {
            newItem.setSoulBound(true);
        }
    }

    /**
     * Add stackable item to inventory
     */
    private static long addStackableItem(Player player, ItemTemplate itemTemplate, long count, ItemAddPredicate predicate, boolean toOverflow) {

        long toadd = count;

        Storage inventory = player.getInventory();
        Collection<Item> items = inventory.getItemsByItemId(itemTemplate.getTemplateId());
        for (Item item : items) {
            if (count == 0) {
                break;
            }
            count = inventory.increaseItemCount(item, count, predicate.getUpdateType(item));
        }

        // dirty & hacky check for arrows and shards...
        if (itemTemplate.getArmorType() == ArmorType.SHARD || itemTemplate.getArmorType() == ArmorType.ARROW) {
            Equipment equipement = player.getEquipment();
            items = equipement.getEquippedItemsByItemId(itemTemplate.getTemplateId());
            for (Item item : items) {
                if (count == 0) {
                    break;
                }
                count = equipement.increaseEquippedItemCount(item, count);
            }
        }


        while ((!inventory.isFull() || toOverflow) && count > 0) {
            Item newItem = ItemFactory.newItem(itemTemplate.getTemplateId(), count);
            count -= newItem.getItemCount();
            inventory.add(newItem);
        }

        long added = toadd - count;

        if (added == 1) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1390000, DescId.of(itemTemplate.getNameId()))); // You have aquired %s
        } else if(added > 0) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1390005, DescId.of(itemTemplate.getNameId()), added)); // You have aquired %s %d(s)
        }

        return count;
    }

    public static boolean addQuestItems(Player player, List<QuestItems> questItems) {
        int needSlot = 0;
        for (QuestItems qi : questItems) {
            if (qi.getItemId() != ItemId.KINAH.value() && qi.getCount() != 0) {
                long stackCount = DataManager.ITEM_DATA.getItemTemplate(qi.getItemId()).getMaxStackCount();
                long count = qi.getCount() / stackCount;
                if (qi.getCount() % stackCount != 0) {
                    count++;
                }
                needSlot += count;
            }
        }
        if (needSlot > player.getInventory().getFreeSlotsQ()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY);
            return false;
        }
        for (QuestItems qi : questItems) {
            addItemQ(player, qi.getItemId(), qi.getCount());
        }
        return true;
    }

    public static void releaseItemId(Item item) {
        IDFactory.getInstance().releaseId(item.getObjectId());
    }

    public static void releaseItemIds(Collection<Item> items) {
        Collection<Integer> idIterator = Collections2.transform(items, AionObject.OBJECT_TO_ID_TRANSFORMER);
        IDFactory.getInstance().releaseIds(idIterator);
    }

    public static boolean dropItemToInventory(int playerObjectId, int itemId) {
        return dropItemToInventory(World.getInstance().findPlayer(playerObjectId), itemId);
    }

    public static boolean dropItemToInventory(Player player, int itemId) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        Storage storage = player.getInventory();
        if (storage.getFreeSlots() < 1) {
            List<Item> items = storage.getItemsByItemId(itemId);
            boolean hasFreeStack = false;
            for (Item item : items) {
                if (item.getPersistentState() == PersistentState.DELETED || item.getItemCount() < item.getItemTemplate().getMaxStackCount()) {
                    hasFreeStack = true;
                    break;
                }
            }
            if (!hasFreeStack) {
                return false;
            }
        }
        return addItem(player, itemId, 1) == 0;
    }

    public static boolean checkRandomTemplate(int randomItemId) {
        ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(randomItemId);
        return template != null;
    }

    public static class ItemAddPredicate implements Predicate<Item> {

        public ItemUpdateType getUpdateType(Item item) {
            return ItemUpdateType.DEFAULT;
        }

        @Override
        public boolean apply(Item input) {
            return true;
        }
    }

}
