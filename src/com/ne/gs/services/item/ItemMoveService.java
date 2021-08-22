/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.item;

import java.util.List;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.IStorage;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.ExchangeService;
import com.ne.gs.services.LegionService;
import com.ne.gs.services.item.ItemPacketService.ItemDeleteType;

import static com.ne.gs.services.item.ItemPacketService.sendItemDeletePacket;
import static com.ne.gs.services.item.ItemPacketService.sendStorageUpdatePacket;
import static com.ne.gs.services.item.ItemRestrictionService.isItemRestrictedFrom;
import static com.ne.gs.services.item.ItemRestrictionService.isItemRestrictedTo;

/**
 * @author ATracer
 */
public final class ItemMoveService {

    public static void moveItem(Player player, int itemObjId, byte sourceStorageType, byte destinationStorageType, short slot) {
        if (ExchangeService.getInstance().isPlayerInExchange(player)) {
            return;
        }

        IStorage sourceStorage = player.getStorage(sourceStorageType);
        Item item = player.getStorage(sourceStorageType).getItemByObjId(itemObjId);

        if (item == null) {
            return;
        }

        if(player.isCasting()) {
        	player.sendPck(SM_SYSTEM_MESSAGE.STR_CANT_EQUIP_ITEM_IN_ACTION);
        	ItemPacketService.sendAllItemsInfo(player);
        	return;
        }

        if (sourceStorageType == destinationStorageType) {
            moveInSameStorage(sourceStorage, item, slot);
            return;
        }

        if ((isItemRestrictedTo(player, item, destinationStorageType) || isItemRestrictedFrom(player, sourceStorageType))) {
            sendStorageUpdatePacket(player, StorageType.getStorageTypeById(sourceStorageType), item);
            return;
        }

        IStorage targetStorage = player.getStorage(destinationStorageType);
        LegionService.getInstance().addWHItemHistory(player, item.getItemId(), item.getItemCount(), sourceStorage, targetStorage);
        if (slot == -1) {
            if (item.getItemTemplate().isStackable()) {
                List<Item> sameItems = targetStorage.getItemsByItemId(item.getItemId());
                for (Item sameItem : sameItems) {
                    long itemCount = item.getItemCount();
                    if (itemCount == 0) {
                        break;
                    }
                    // we can merge same stackable items
                    ItemSplitService.mergeStacks(sourceStorage, targetStorage, item, sameItem, itemCount);
                }
            }
        }
        if (!targetStorage.isFull() && item.getItemCount() > 0) {
            sourceStorage.remove(item);
            sendItemDeletePacket(player, StorageType.getStorageTypeById(sourceStorageType), item, ItemDeleteType.MOVE);
            item.setEquipmentSlot(slot);
            targetStorage.add(item);
        }
    }

    /**
     * @param storage
     * @param item
     * @param slot
     */
    private static void moveInSameStorage(IStorage storage, Item item, short slot) {
        storage.setPersistentState(PersistentState.UPDATE_REQUIRED);
        item.setEquipmentSlot(slot);
        item.setPersistentState(PersistentState.UPDATE_REQUIRED);
    }

    public static void switchItemsInStorages(Player player, byte sourceStorageType, int sourceItemObjId, byte replaceStorageType,
                                             int replaceItemObjId) {
        IStorage sourceStorage = player.getStorage(sourceStorageType);
        IStorage replaceStorage = player.getStorage(replaceStorageType);

        Item sourceItem = sourceStorage.getItemByObjId(sourceItemObjId);
        if (sourceItem == null) {
            return;
        }

        if(player.isCasting()) {
        	player.sendPck(SM_SYSTEM_MESSAGE.STR_CANT_EQUIP_ITEM_IN_ACTION);
        	ItemPacketService.updateItemAfterInfoChange(player, sourceItem);
        	return;
        }

        Item replaceItem = replaceStorage.getItemByObjId(replaceItemObjId);
        if (replaceItem == null) {
            return;
        }

        // restrictions checks
        if (isItemRestrictedFrom(player, sourceStorageType)
            || isItemRestrictedFrom(player, replaceStorageType)
            || isItemRestrictedTo(player, sourceItem, replaceStorageType)
            || isItemRestrictedTo(player, replaceItem, sourceStorageType)) {
            return;
        }

        int sourceSlot = sourceItem.getEquipmentSlot();
        int replaceSlot = replaceItem.getEquipmentSlot();

        sourceItem.setEquipmentSlot(replaceSlot);
        replaceItem.setEquipmentSlot(sourceSlot);

        sourceStorage.remove(sourceItem);
        replaceStorage.remove(replaceItem);

        // correct UI update order is 1)delete items 2) add items
        sendItemDeletePacket(player, StorageType.getStorageTypeById(sourceStorageType), sourceItem, ItemDeleteType.MOVE);
        sendItemDeletePacket(player, StorageType.getStorageTypeById(replaceStorageType), replaceItem, ItemDeleteType.MOVE);
        sourceStorage.add(replaceItem);
        replaceStorage.add(sourceItem);
    }
}
