/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team.legion;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.services.item.ItemPacketService.ItemDeleteType;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;

/**
 * @author Simple
 */
public class LegionWarehouse extends Storage {

    private Legion legion;
    private int curentWhUser;

    public LegionWarehouse(Legion legion) {
        super(StorageType.LEGION_WAREHOUSE);
        this.legion = legion;
        setLimit(legion.getWarehouseSlots());
    }

    public Legion getLegion() {
        return legion;
    }

    public void setOwnerLegion(Legion legion) {
        this.legion = legion;
    }

    @Override
    public void increaseKinah(long amount) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public void increaseKinah(long amount, ItemUpdateType updateType) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public boolean tryDecreaseKinah(long amount) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public void decreaseKinah(long amount) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public void decreaseKinah(long amount, ItemUpdateType updateType) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public long increaseItemCount(Item item, long count) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public long increaseItemCount(Item item, long count, ItemUpdateType updateType) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public long decreaseItemCount(Item item, long count) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public long decreaseItemCount(Item item, long count, ItemUpdateType updateType) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public Item add(Item item) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public Item put(Item item) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public Item delete(Item item) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public Item delete(Item item, ItemDeleteType deleteType) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public boolean decreaseByItemId(int itemId, long count) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public boolean decreaseByObjectId(int itemObjId, long count) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public boolean decreaseByObjectId(int itemObjId, long count, ItemUpdateType updateType) {
        throw new UnsupportedOperationException("LWH should be used behind proxy");
    }

    @Override
    public void setOwner(Player player) {
        throw new UnsupportedOperationException("LWH doesnt have owner");
    }

    public void setWhUser(int curentWhUser) {
        this.curentWhUser = curentWhUser;
    }

    public int getWhUser() {
        return curentWhUser;
    }
}
