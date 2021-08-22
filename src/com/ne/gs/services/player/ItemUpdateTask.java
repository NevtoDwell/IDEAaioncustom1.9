/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.InventoryDAO;
import com.ne.gs.database.dao.ItemStoneListDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.world.World;

class ItemUpdateTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ItemUpdateTask.class);
    private final int playerId;

    ItemUpdateTask(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public void run() {
        Player player = World.getInstance().findPlayer(playerId);
        if (player != null) {
            try {
                GDB.get(InventoryDAO.class).store(player);
                GDB.get(ItemStoneListDAO.class).save(player);
            } catch (Exception ex) {
                log.error("Exception during periodic saving of player items " + player.getName(), ex);
            }
        }
    }
}
