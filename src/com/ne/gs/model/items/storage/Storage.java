/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.items.storage;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.common.collect.ImmutableList;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.ItemId;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.item.ItemFactory;
import com.ne.gs.services.item.ItemPacketService;
import com.ne.gs.services.item.ItemPacketService.ItemDeleteType;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;

/**
 * @author KID, ATracer
 */
public abstract class Storage implements IStorage {

    private final ItemStorage itemStorage;
    private Item kinahItem;
    private final StorageType storageType;
    private Queue<Item> deletedItems;
    /** npc.getMoveController()
     * Can be of 2 types: UPDATED and UPDATE_REQUIRED
     */
    private PersistentState persistentState = PersistentState.UPDATED;

    public Storage(StorageType storageType) {
        this(storageType, true);
    }

    public Storage(StorageType storageType, boolean withDeletedItems) {
        itemStorage = new ItemStorage(storageType.getLimit());
        this.storageType = storageType;
        if (withDeletedItems) {
            deletedItems = new ConcurrentLinkedQueue<>();
        }
    }

    @Override
    public long getKinah() {
        return kinahItem == null ? 0 : kinahItem.getItemCount();
    }

    @Override
    public Item getKinahItem() {
        return kinahItem;
    }

    @Override
    public StorageType getStorageType() {
        return storageType;
    }

    void increaseKinah(long amount, Player actor) {
        increaseKinah(amount, ItemUpdateType.INC_KINAH_LOOT, actor);
    }

    void increaseKinah(long amount, ItemUpdateType updateType, Player actor) {
        if (amount > 0) {
            if (kinahItem == null) {
                add(ItemFactory.newItem(ItemId.KINAH.value(), amount), actor);
            } else {
                increaseItemCount(kinahItem, amount, updateType, actor);
            }
        }
    }

    /**
     * Decrease kinah by {@code amount} but check first that its enough in storage
     *
     * @return true if decrease was successful
     */
    boolean tryDecreaseKinah(long amount, Player actor) {
        if (getKinah() >= amount) {
            decreaseKinah(amount, actor);
            return true;
        }
        return false;
    }

    /**
     * just decrease kinah without any checks
     */
    void decreaseKinah(long amount, Player actor) {
        decreaseKinah(amount, ItemUpdateType.DEC_KINAH, actor);
    }

    void decreaseKinah(long amount, ItemUpdateType updateType, Player actor) {
        if (amount > 0) {
            decreaseItemCount(kinahItem, amount, updateType, actor);
        }
    }

    long increaseItemCount(Item item, long count, Player actor) {
        return increaseItemCount(item, count, ItemUpdateType.DEFAULT, actor);
    }

    /**
     * increase item count and return left count
     */
    long increaseItemCount(Item item, long count, ItemUpdateType updateType, Player actor) {
        long leftCount = item.increaseItemCount(count);
        ItemPacketService.sendItemPacket(actor, storageType, item, updateType);
        setPersistentState(PersistentState.UPDATE_REQUIRED);
        return leftCount;
    }

    long decreaseItemCount(Item item, long count, Player actor) {
        return this.decreaseItemCount(item, count, ItemUpdateType.DEFAULT, actor);
    }

    /**
     * decrease item count and return left count
     */
    long decreaseItemCount(Item item, long count, ItemUpdateType updateType, Player actor) {
        if (item == null) {
            return 0;
        }
        long leftCount = item.decreaseItemCount(count);
        if (item.getItemCount() <= 0 && !item.getItemTemplate().isKinah()) {
            delete(item, ItemDeleteType.fromUpdateType(updateType), actor);
        } else {
            ItemPacketService.sendItemPacket(actor, storageType, item, updateType);
        }
        setPersistentState(PersistentState.UPDATE_REQUIRED);
        return leftCount;
    }

    /**
     * This method should be called only for new items added to inventory (loading from DB) If item is equiped - will be put to equipment if item is unequiped -
     * will be put to default bag for now Kinah is stored separately as it will be used frequently
     *
     * @param item
     */
    @Override
    public void onLoadHandler(Item item) {
        if (item.getItemTemplate().isKinah()) {
            kinahItem = item;
        } else {
            itemStorage.putItem(item);
        }
    }

    Item add(Item item, Player actor) {
        if (item.getItemTemplate().isKinah()) {
            kinahItem = item;
        } else if (!itemStorage.putItem(item)) {
            return null;
        }
        item.setItemLocation(storageType.getId());
        setPersistentState(PersistentState.UPDATE_REQUIRED);
        ItemPacketService.sendStorageUpdatePacket(actor, storageType, item);
        // TODO: move to ItemService
        QuestEngine.getInstance().onItemGet(new QuestEnv(null, actor, 0, 0), item.getItemTemplate().getTemplateId());
        return item;
    }

    // a bit misleading name - but looks like its used only for equipment
    Item put(Item item, Player actor) {
        if (!itemStorage.putItem(item)) {
            return null;
        }
        item.setItemLocation(storageType.getId());
        setPersistentState(PersistentState.UPDATE_REQUIRED);
        ItemPacketService.sendItemUpdatePacket(actor, storageType, item, ItemUpdateType.EQUIP_UNEQUIP);
        return item;
    }

    /**
     * Remove item from storage without changing its state
     */
    @Override
    public Item remove(Item item) {
        return itemStorage.removeItem(item.getObjectId());
    }

    /**
     * Delete item from storage and mark for DB update. UNKNOWN delete type
     */
    Item delete(Item item, Player actor) {
        return delete(item, ItemDeleteType.UNKNOWN, actor);
    }

    /**
     * Delete item from storage and mark for DB update
     */
    Item delete(Item item, ItemDeleteType deleteType, Player actor) {
        if (remove(item) != null) {
            item.setPersistentState(PersistentState.DELETED);
            deletedItems.add(item);
            setPersistentState(PersistentState.UPDATE_REQUIRED);
            ItemPacketService.sendItemDeletePacket(actor, StorageType.getStorageTypeById(item.getItemLocation()), item, deleteType);
            return item;
        }
        return null;
    }

    boolean decreaseByItemId(int itemId, long count, Player actor) {
        ImmutableList<Item> items = itemStorage.getItemsById(itemId);
        if (items.size() == 0) {
            return false;
        }

        for (Item item : items) {
            if (count == 0) {
                break;
            }
            count = decreaseItemCount(item, count, actor);
        }

        return count == 0;
    }

    boolean decreaseByObjectId(int itemObjId, long count, Player actor) {
        return decreaseByObjectId(itemObjId, count, ItemUpdateType.DEFAULT, actor);
    }

    boolean decreaseByObjectId(int itemObjId, long count, ItemUpdateType updateType, Player actor) {
        Item item = itemStorage.getItemByObjId(itemObjId);
        if (item == null || item.getItemCount() < count) {
            return false;
        }

        return decreaseItemCount(item, count, updateType, actor) == 0;
    }

    @Override
    public Item getFirstItemByItemId(int itemId) {
        return itemStorage.getFirstItemById(itemId);
    }

    @Override
    public ImmutableList<Item> getItemsWithKinah() {
        ImmutableList<Item> items = itemStorage.getItems();
        if (kinahItem != null) {
            return ImmutableList.<Item>builder().addAll(items).add(kinahItem).build();
        }
        return items;
    }

    @Override
    public ImmutableList<Item> getItems() {
        return itemStorage.getItems();
    }

    @Override
    public ImmutableList<Item> getItemsByItemId(int itemId) {
        return itemStorage.getItemsById(itemId);
    }

    @Override
    public Queue<Item> getDeletedItems() {
        return deletedItems;
    }

    @Override
    public Item getItemByObjId(int itemObjId) {
        return itemStorage.getItemByObjId(itemObjId);
    }

    @Override
    public long getItemCountByItemId(int itemId) {
        ImmutableList<Item> temp = itemStorage.getItemsById(itemId);
        if (temp.size() == 0) {
            return 0;
        }

        long cnt = 0;
        for (Item item : temp) {
            cnt += item.getItemCount();
        }

        return cnt;
    }
	
	public boolean hasItem(int itemId) {
        return getItemCountByItemId(itemId) > 0;
    }

    @Override
    public boolean isFull() {
        return itemStorage.isFull();
    }

    @Override
    public int getFreeSlots() {
        return itemStorage.getFreeSlots();
    }

    public boolean setLimit(int limit) {
        return itemStorage.setLimit(limit);
    }

    @Override
    public int getLimit() {
        return itemStorage.getLimit();
    }

    @Override
    public final PersistentState getPersistentState() {
        return persistentState;
    }

    @Override
    public final void setPersistentState(PersistentState persistentState) {
        this.persistentState = persistentState;
    }

    @Override
    public int size() {
        return itemStorage.size();
    }
    
    public boolean isFullQ() {
        return this.itemStorage.isFullQ();
    }

    public int getFreeSlotsQ() {
        return this.itemStorage.getFreeSlotsQ();
    }
}
