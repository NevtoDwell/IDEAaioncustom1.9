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
public class PlayerMoveTaskManager extends AbstractPeriodicTaskManager {

    private final ConcurrentHashMap<Integer, Creature> movingPlayers = new ConcurrentHashMap<>();

    private PlayerMoveTaskManager() {
        super(200);
    }

    public void addPlayer(Creature player) {
        movingPlayers.put(player.getObjectId(), player);
    }

    public void removePlayer(Creature player) {
        movingPlayers.remove(player.getObjectId());
    }

    @Override
    public void run() {
        for (ConcurrentHashMap.Entry<Integer, Creature> e : movingPlayers.entrySet()) {
            Creature player = e.getValue();
            player.getMoveController().moveToDestination();
        }
    }

    public static final PlayerMoveTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {

        private static final PlayerMoveTaskManager INSTANCE = new PlayerMoveTaskManager();
    }
}
