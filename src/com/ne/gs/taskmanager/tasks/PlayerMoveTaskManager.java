/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager.tasks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.taskmanager.AbstractPeriodicTaskManager;

/**
 * @author ATracer
 */
public final class PlayerMoveTaskManager {

    private static final ConcurrentMap<Integer, Creature> _moving = new ConcurrentHashMap<>(16, 0.75f, 8);

    static {
        new AbstractPeriodicTaskManager(200) {
            @Override
            public void run() {
                for (Creature c : _moving.values()) {
                    c.getMoveController().moveToDestination();
                }
            }
        };
    }

    public static void addPlayer(Creature player) {
        _moving.put(player.getObjectId(), player);
    }

    public static void removePlayer(Creature player) {
        _moving.remove(player.getObjectId());
    }
}
