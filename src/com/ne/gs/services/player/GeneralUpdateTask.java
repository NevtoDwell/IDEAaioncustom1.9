/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.player;

import com.ne.gs.database.GDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.dao.AbyssRankDAO;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.database.dao.PlayerQuestListTable;
import com.ne.gs.database.dao.PlayerSkillListDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.world.World;

class GeneralUpdateTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(GeneralUpdateTask.class);
    private final int playerId;

    GeneralUpdateTask(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public void run() {
        Player player = World.getInstance().findPlayer(playerId);
        if (player != null) {
            try {
                GDB.get(AbyssRankDAO.class).storeAbyssRank(player);
                GDB.get(PlayerSkillListDAO.class).storeSkills(player);
                PlayerQuestListTable.store(player);
                GDB.get(PlayerDAO.class).storePlayer(player);
            } catch (Exception ex) {
                log.error("Exception during periodic saving of player " + player.getName(), ex);
            }
        }
    }
}
