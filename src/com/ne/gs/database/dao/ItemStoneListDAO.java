/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.ManaStone;

/**
 * @author ATracer modified by Wakizashi
 */
public abstract class ItemStoneListDAO implements DAO {

    /**
     * Loads stones of item
     *
     * @param items
     *     list of items to load stones
     */
    public abstract void load(Collection<Item> items);

    public abstract void storeManaStones(Set<ManaStone> manaStones);

    public abstract void storeFusionStone(Set<ManaStone> fusionStones);

    /**
     * Saves stones of player
     *
     * @param player
     *     whos stones we need to save
     */
    public void save(Player player) {
        save(player.getAllItems());
    }

    public abstract void save(List<Item> items);

    @Override
    public String getClassName() {
        return ItemStoneListDAO.class.getName();
    }
}
