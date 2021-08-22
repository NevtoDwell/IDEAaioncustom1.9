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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;

/**
 * @author ATracer
 */
public class DebugService {

    private static final Logger log = LoggerFactory.getLogger(DebugService.class);

    private static final int ANALYZE_PLAYERS_INTERVAL = 30 * 60 * 1000;

    public static DebugService getInstance() {
        return SingletonHolder.instance;
    }

    private DebugService() {
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                analyzeWorldPlayers();
            }

        }, ANALYZE_PLAYERS_INTERVAL, ANALYZE_PLAYERS_INTERVAL);
        log.info("DebugService started. Analyze iterval: " + ANALYZE_PLAYERS_INTERVAL);
    }

    private void analyzeWorldPlayers() {
        log.info("Starting analysis of world players at " + System.currentTimeMillis());

        Iterator<Player> playersIterator = World.getInstance().getPlayersIterator();
        while (playersIterator.hasNext()) {
            Player player = playersIterator.next();

            /**
             * Check connection
             */
            AionConnection connection = player.getClientConnection();
            if (connection == null) {
                log.warn(String.format("[DEBUG SERVICE] Player without connection: " + "detected: ObjId %d, Name %s, Spawned %s", player.getObjectId(),
                    player.getName(), player.isSpawned()));
                continue;
            }

            /**
             * Check CM_PING packet
             */
            long lastPingTimeMS = connection.getLastPingTimeMS();
            long pingInterval = System.currentTimeMillis() - lastPingTimeMS;
            if (lastPingTimeMS > 0 && pingInterval > 300000) {
                log.warn(String.format("[DEBUG SERVICE] Player with large ping interval: " + "ObjId %d, Name %s, Spawned %s, PingMS %d", player.getObjectId(),
                    player.getName(), player.isSpawned(), pingInterval));
            }
        }
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final DebugService instance = new DebugService();
    }
}
