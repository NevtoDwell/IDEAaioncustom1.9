/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.player;

import javolution.util.FastMap;

import com.ne.commons.services.CronService;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.model.SellLimit;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author Source
 */
public class PlayerLimitService {

    private static final FastMap<Integer, Long> sellLimit = new FastMap<Integer, Long>().shared();

    public static boolean updateSellLimit(Player player, long reward) {
        if (!CustomConfig.LIMITS_ENABLED) {
            return true;
        }

        int accoutnId = player.getPlayerAccount().getId();
        Long limit = sellLimit.get(accoutnId);
        if (limit == null) {
            limit = SellLimit.getSellLimit(player.getPlayerAccount().getMaxPlayerLevel()) * CustomConfig.LIMITS_RATE;
            sellLimit.put(accoutnId, limit);
        }

        if (limit < reward) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DAY_CANNOT_SELL_NPC(limit));
            return false;
        } else {
            limit -= reward;
            sellLimit.putEntry(accoutnId, limit);
            return true;
        }
    }

    public void scheduleUpdate() {
        CronService.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                sellLimit.clear();
            }

        }, CustomConfig.LIMITS_UPDATE, true);
    }

    public static PlayerLimitService getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder {

        protected static final PlayerLimitService instance = new PlayerLimitService();
    }

}
