/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_USE_OBJECT;

/**
 * @author hex1r0
 */
public class DelayedGameAction {

    private final Player _player;
    private final int _duration;
    private final Runnable _task = new Task();

    public DelayedGameAction(Player player, int duration) {
        _player = player;
        _duration = duration;
    }

    public Player getPlayer() {
        return _player;
    }

    public int getDuration() {
        return _duration;
    }

    public final void invoke() {
        preRun();
        _player.sendPck(new SM_USE_OBJECT(_player.getObjectId(), 0, _duration, 1));
        ThreadPoolManager.getInstance().schedule(_task, _duration);
    }

    private class Task implements Runnable {

        @Override
        public void run() {
            _player.sendPck(new SM_USE_OBJECT(_player.getObjectId(), 0, _duration, 2));
            postRun();
        }
    }

    protected void preRun() {

    }

    protected void postRun() {

    }
}
