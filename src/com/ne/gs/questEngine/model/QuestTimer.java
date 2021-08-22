/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.model;

import java.util.Timer;
import java.util.TimerTask;

import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author Hilgert
 */
public class QuestTimer {

    private Timer timer;

    private int Time = 0;

    @SuppressWarnings("unused")
    private final int questId;

    private boolean isTicking = false;

    private final Player player;

    /**
     * @param questId
     */
    public QuestTimer(int questId, int seconds, Player player) {
        this.questId = questId;
        this.Time = seconds * 1000;
        this.player = player;
    }

    /**
     * @return
     */
    public void Start() {
        player.sendMsg("Timer started");
        timer = new Timer();
        isTicking = true;
        // TODO Send Packet that timer start
        TimerTask task = new TimerTask() {

            public void run() {
                player.sendMsg("Timer is over");
                onEnd();
            }
        };

        timer.schedule(task, Time);
    }

    public void Stop() {
        timer.cancel();
        onEnd();
    }

    public void onEnd() {
        // TODO Send Packet that timer end
        isTicking = false;
    }

    /**
     * @return false - if Timer not started or stoped.
     */
    public boolean isTicking() {
        return this.isTicking;
    }

    /**
     * @return
     */
    public int getTimeSeconds() {
        return this.Time / 1000;
    }
}
