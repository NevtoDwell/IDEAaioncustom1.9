/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.gametime;

/**
 * Responsible for updating the clock
 *
 * @author Ben
 */
public class GameTimeUpdater implements Runnable {

    private final GameTime time;

    /**
     * Constructs GameTimeUpdater to update the given GameTime
     *
     * @param time
     *     GameTime to update
     */
    public GameTimeUpdater(GameTime time) {
        this.time = time;
    }

    /**
     * Increases the time by one minute
     */
    @Override
    public void run() {
        time.increase();
    }
}
