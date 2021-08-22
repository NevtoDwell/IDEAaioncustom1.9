/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.network.util.ThreadPoolManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_GAME_TIME;
import com.ne.gs.utils.gametime.GameTimeManager;
import com.ne.gs.world.World;

/**
 * @author ATracer
 */
public final class GameTimeService {

    private static final Logger log = LoggerFactory.getLogger(GameTimeService.class);

    public static GameTimeService getInstance() {
        return SingletonHolder.instance;
    }

    private final static int GAMETIME_UPDATE = 3 * 60000;

    private GameTimeService() {
        /**
         * Update players with current game time
         */
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                log.info("Sending current game time to all players");
                Iterator<Player> iterator = World.getInstance().getPlayersIterator();
                while (iterator.hasNext()) {
                    Player next = iterator.next();
                    next.sendPck(new SM_GAME_TIME());
                }
                GameTimeManager.saveTime();
            }
        }, GAMETIME_UPDATE, GAMETIME_UPDATE);

        log.info("GameTimeService started. Update interval:" + GAMETIME_UPDATE);
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final GameTimeService instance = new GameTimeService();
    }
}
