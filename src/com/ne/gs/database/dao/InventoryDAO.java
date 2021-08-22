/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.Collections;
import java.util.List;
import javolution.util.FastList;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Equipment;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.items.storage.StorageType;

/**
 * @author ATracer
 */
public abstract class InventoryDAO implements IDFactoryAwareDAO {

    /**
     * @param playerId
     * @param storageType
     *
     * @return IStorage
     */
    public abstract Storage loadStorage(int playerId, StorageType storageType);

    public abstract List<Item> loadStorageDirect(int playerId, StorageType storageType);

    /**
     * @param player
     *
     * @return Equipment
     */
    public abstract Equipment loadEquipment(Player player);

    /**
     * @param playerId
     *
     * @return
     */
    public abstract List<Item> loadEquipment(int playerId);

    public abstract boolean store(Player player);

    public abstract boolean store(Item item, Player player);

    public boolean store(Item item, int playerId) {
        return store(Collections.singletonList(item), playerId);
    }

    public abstract boolean store(List<Item> items, int playerId);

    /**
     * @param item
     */
    public boolean store(Item item, Integer playerId, Integer accountId, Integer legionId) {
        FastList<Item> temp = FastList.newInstance();
        temp.add(item);
        return store(temp, playerId, accountId, legionId);
    }

    public abstract boolean store(List<Item> items, Integer playerId, Integer accountId, Integer legionId);

    /**
     * @param playerId
     */
    public abstract boolean deletePlayerItems(int playerId);

    public abstract void deleteAccountWH(int accountId);

    @Override
    public String getClassName() {
        return InventoryDAO.class.getName();
    }
}
