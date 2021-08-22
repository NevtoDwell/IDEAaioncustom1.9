/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.gametime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.ServerVariablesDAO;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * Manages ingame time
 *
 * @author Ben
 */
public final class GameTimeManager {

    private static final Logger log = LoggerFactory.getLogger(GameTimeManager.class);
    private static GameTime instance;
    private static GameTimeUpdater updater;
    private static boolean clockStarted = false;

    static {
        ServerVariablesDAO dao = GDB.get(ServerVariablesDAO.class);
        instance = new GameTime(dao.load("time"));
    }

    /**
     * Gets the current GameTime
     *
     * @return GameTime
     */
    public static GameTime getGameTime() {
        return instance;
    }

    /**
     * Starts the counter that increases the clock every tick
     *
     * @throws IllegalStateException
     *     If called twice
     */
    public static void startClock() {
        if (clockStarted) {
            throw new IllegalStateException("Clock is already started");
        }

        updater = new GameTimeUpdater(getGameTime());
        ThreadPoolManager.getInstance().scheduleAtFixedRate(updater, 0, 5000);

        clockStarted = true;
    }

    /**
     * Saves the current time to the database
     *
     * @return Success
     */
    public static boolean saveTime() {
        log.info("Game time saved...");
        return GDB.get(ServerVariablesDAO.class).store("time", getGameTime().getTime());
    }

    /**
     * Clean scheduled queues, set a new GameTime, then restart the clock
     */
    public static void reloadTime(int time) {
        ThreadPoolManager.getInstance().purge();
        instance = new GameTime(time);

        clockStarted = false;

        startClock();
        log.info("Game time changed by admin and clock restarted...");
    }
}
