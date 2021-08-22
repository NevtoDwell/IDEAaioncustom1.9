/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.instance;

import org.slf4j.LoggerFactory;

import com.ne.commons.DateUtil;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

public final class PvPArenaService {

    public static boolean isPvPArenaAvailable(Player player, int mapId) {
        if (!isPvPArenaAvailable()) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1401306, mapId));
            return false;
        }

//        if (CustomConfig.ARENA_TICKET_CHECK && player.getInventory().getFirstItemByItemId(186000135) == null) {
//            player.sendPck(new SM_SYSTEM_MESSAGE(1400219, mapId));
//            return false;
//        }

        return true;
    }

    private static boolean isPvPArenaAvailable() {
        // TODO optimize
        try {
            String[] tokens = CustomConfig.ARENA_TIME.split(";\\s*");
            for (String tok : tokens) {
                String[] expr = tok.split("\\s*<>\\s*");
                if (DateUtil.cronBetween(expr[0], expr[1])) {
                    return true;
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(PvPArenaService.class).warn("Unable to parse arena time", e);
        }

        return false;
    }
}
