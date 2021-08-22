/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.items.storage;

import java.util.List;
import java.util.Queue;
import com.google.common.collect.ImmutableList;
import javolution.util.FastList;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.item.ItemPacketService.ItemDeleteType;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;

/**
 * Public interface for Storage, later will rename probably
 *
 * @author ATracer
 */
public interface IStorage {

    /**
     * @param player
     */
    void setOwner(Player player);

    /**
     * @return current kinah count
     */
    long getKinah();

    /**
     * @return kinah item or null if storage never had kinah
     */
    Item getKinahItem();

    /**
     * @return
     */
    StorageType getStorageType();

    /**
     * @param amount
     */
    void increaseKinah(long amount);

    /**
     * @param amount
     * @param updateType
     */
    void increaseKinah(long amount, ItemUpdateType updateType);

    /**
     * @param amount
     *
     * @return
     */
    boolean tryDecreaseKinah(long amount);

    /**
     * @param amount
     */
    void decreaseKinah(long amount);

    /**
     * @param amount
     * @param updateType
     */
    void decreaseKinah(long amount, ItemUpdateType updateType);

    /**
     * @param item
     * @param count
     *
     * @return
     */
    long increaseItemCount(Item item, long count);

    /**
     * @param item
     * @param count
     * @param updateType
     *
     * @return
     */
    long increaseItemCount(Item item, long count, ItemUpdateType updateType);

    /**
     * @param item
     * @param count
     *
     * @return
     */
    long decreaseItemCount(Item item, long count);

    /**
     * @param item
     * @param count
     * @param updateType
     *
     * @return
     */
    long decreaseItemCount(Item item, long count, ItemUpdateType updateType);

    /**
     * Add operation should be used for new items incoming into storage from outside
     */
    Item add(Item item);

    /**
     * Put operation is used in some operations like unequip
     */
    Item put(Item item);

    /**
     * @param item
     *
     * @return
     */
    Item remove(Item item);

    /**
     * @param item
     *
     * @return
     */
    Item delete(Item item);

    /**
     * @param item
     * @param deleteType
     *
     * @return
     */
    Item delete(Item item, ItemDeleteType deleteType);

    /**
     * @param itemId
     * @param count
     *
     * @return
     */
    boolean decreaseByItemId(int itemId, long count);

    /**
     * @param itemObjId
     * @param count
     *
     * @return
     */
    boolean decreaseByObjectId(int itemObjId, long count);

    /**
     * @param itemObjId
     * @param count
     * @param updateType
     *
     * @return
     */
    boolean decreaseByObjectId(int itemObjId, long count, ItemUpdateType updateType);

    /**
     * @param itemId
     *
     * @return
     */
    Item getFirstItemByItemId(int itemId);

    /**
     * @return
     */
    ImmutableList<Item> getItemsWithKinah();

    /**
     * @return
     */
    ImmutableList<Item> getItems();

    /**
     *
     * @param itemId
     *
     * @return
     */
    ImmutableList<Item> getItemsByItemId(int itemId);

    /**
     * @param itemObjId
     *
     * @return
     */
    Item getItemByObjId(int itemObjId);

    /**
     * @param itemId
     *
     * @return
     */
    long getItemCountByItemId(int itemId);

    /**
     * @return
     */
    boolean isFull();

    /**
     * @return
     */
    int getFreeSlots();

    /**
     * @return
     */
    int getLimit();

    /**
     * @return
     */
    int size();

    /**
     * @return
     */
    PersistentState getPersistentState();

    /**
     * @param persistentState
     */
    void setPersistentState(PersistentState persistentState);

    /**
     * @return
     */
    Queue<Item> getDeletedItems();

    /**
     * @param item
     */
    void onLoadHandler(Item item);

}
