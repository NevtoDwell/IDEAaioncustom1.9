/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.IStorage;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.ExchangeService;
import com.ne.gs.services.LegionService;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;

import static com.ne.gs.services.item.ItemPacketService.sendStorageUpdatePacket;
import static com.ne.gs.services.item.ItemRestrictionService.isItemRestrictedFrom;
import static com.ne.gs.services.item.ItemRestrictionService.isItemRestrictedTo;

/**
 * @author ATracer
 */
public final class ItemSplitService {

    private static final Logger log = LoggerFactory.getLogger(ItemSplitService.class);

    /**
     * Move part of stack into different slot
     */
    public static void splitItem(Player player, int itemObjId, int destinationObjId, long splitAmount, short slotNum,
                                 byte sourceStorageType, byte destinationStorageType) {
        if (splitAmount <= 0) {
            return;
        }
        if (player.isTrading()) {
            // You cannot split items in the inventory during a trade.
            player.sendPck(new SM_SYSTEM_MESSAGE(1300713));
            return;
        }

        IStorage sourceStorage = player.getStorage(sourceStorageType);
        IStorage destStorage = player.getStorage(destinationStorageType);
        if (sourceStorage == null || destStorage == null) {
            log.warn(String.format("storage null playerName sourceStorage destStorage %s %d %d", player.getName(), sourceStorageType, destinationStorageType));
            return;
        }
        Item sourceItem = sourceStorage.getItemByObjId(itemObjId);
        Item targetItem = destStorage.getItemByObjId(destinationObjId);

        if (sourceItem == null) {
            sourceItem = sourceStorage.getKinahItem();
            if (sourceItem == null || sourceItem.getObjectId() != itemObjId) {
                log.warn(String.format("CHECKPOINT: attempt to split null item %d %d %d", itemObjId, splitAmount, slotNum));
                return;
            }
        }

        if (sourceStorageType != destinationStorageType
            && (isItemRestrictedTo(player, sourceItem, destinationStorageType) || isItemRestrictedFrom(player, sourceStorageType))) {
            sendStorageUpdatePacket(player, StorageType.getStorageTypeById(sourceStorageType), sourceItem);
            return;
        }

        // To move kinah from inventory to warehouse and vice versa client using split item packet
        if (sourceItem.getItemTemplate().isKinah()) {
            moveKinah(player, sourceStorage, sourceItem, splitAmount);
            return;
        }

        if (targetItem == null) {
            long oldItemCount = sourceItem.getItemCount() - splitAmount;
            if (sourceItem.getItemCount() < splitAmount || oldItemCount == 0) {
                return;
            }
            if (sourceStorageType != destinationStorageType) {
                LegionService.getInstance().addWHItemHistory(player, sourceItem.getItemId(), splitAmount, sourceStorage, destStorage);
            }
            Item newItem = ItemFactory.newItem(sourceItem.getItemTemplate().getTemplateId(), splitAmount);
            newItem.setEquipmentSlot(slotNum);
            sourceStorage.decreaseItemCount(sourceItem, splitAmount, ItemUpdateType.DEC_SPLIT);
            player.sendPck(SM_CUBE_UPDATE.cubeSize(sourceStorage.getStorageType(), player));
            if (destStorage.add(newItem) == null) {
                // if item was not added - we can release its id
                ItemService.releaseItemId(newItem);
            }
        } else if (targetItem.getItemId() == sourceItem.getItemId()) {
            if (sourceStorageType != destinationStorageType) {
                LegionService.getInstance().addWHItemHistory(player, sourceItem.getItemId(), splitAmount, sourceStorage, destStorage);
            }
            mergeStacks(sourceStorage, destStorage, sourceItem, targetItem, splitAmount);
        }
    }

    /**
     * Merge 2 stacks with simple validation validation
     */
    public static void mergeStacks(IStorage sourceStorage, IStorage destStorage, Item sourceItem, Item targetItem, long count) {
        if (sourceItem.getItemCount() >= count) {
            long freeCount = targetItem.getFreeCount();
            count = count > freeCount ? freeCount : count;
            long leftCount = destStorage.increaseItemCount(targetItem, count, ItemUpdateType.INC_MERGE);
            sourceStorage.decreaseItemCount(sourceItem, count - leftCount, ItemUpdateType.DEC_SPLIT);
        }

    }

    private static void moveKinah(Player player, IStorage source, Item sourceItem, long splitAmount) {
        if (source.getKinah() < splitAmount) {
            return;
        }
        if (ExchangeService.getInstance().isPlayerInExchange(player)) {
            return;
        }

        switch (source.getStorageType()) {
            case CUBE: {
                IStorage destination = player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId());
                long chksum = (source.getKinah() - splitAmount) + (destination.getKinah() + splitAmount);

                if (chksum != source.getKinah() + destination.getKinah()) {
                    return;
                }

                updateKinahCount(source, splitAmount, destination);
                break;
            }

            case ACCOUNT_WAREHOUSE: {
                IStorage destination = player.getStorage(StorageType.CUBE.getId());
                long chksum = (source.getKinah() - splitAmount) + (destination.getKinah() + splitAmount);

                if (chksum != source.getKinah() + destination.getKinah()) {
                    return;
                }

                updateKinahCount(source, splitAmount, destination);
                break;
            }

            //            case LEGION_WAREHOUSE: {
            //                if (!isItemRestrictedFrom(player, sourceItem, source.getStorageType().getId())
            //
            //            }
        }
    }

    private static void updateKinahCount(IStorage source, long splitAmount, IStorage destination) {
        source.decreaseKinah(splitAmount, ItemUpdateType.DEC_SPLIT);
        destination.increaseKinah(splitAmount, ItemUpdateType.INC_MERGE_KINAH);
    }
}
