/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager.tasks;

import java.util.Map;
import javolution.util.FastMap;

import com.ne.gs.model.IExpirable;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.taskmanager.AbstractPeriodicTaskManager;

/**
 * @author Mr. Poke
 */
public class ExpireTimerTask extends AbstractPeriodicTaskManager {

    private final FastMap<IExpirable, Player> expirables = new FastMap<>();

    /**
     */
    public ExpireTimerTask() {
        super(1000);
    }

    public static ExpireTimerTask getInstance() {
        return SingletonHolder._instance;
    }

    public void addTask(IExpirable expirable, Player player) {
        writeLock();
        try {
            expirables.put(expirable, player);
        } finally {
            writeUnlock();
        }
    }

    public void removePlayer(Player player) {
        writeLock();
        try {
            for (Map.Entry<IExpirable, Player> entry : expirables.entrySet()) {
                if (entry.getValue() == player) {
                    expirables.remove(entry.getKey());
                }
            }
        } finally {
            writeUnlock();
        }
    }

    @Override
    public void run() {
        writeLock();
        try {
            int timeNow = (int) (System.currentTimeMillis() / 1000);
            for (Map.Entry<IExpirable, Player> entry : expirables.entrySet()) {
                IExpirable expirable = entry.getKey();
                Player player = entry.getValue();
                int min = (expirable.getExpireTime() - timeNow);
                if (min < 0 && expirable.canExpireNow()) {
                    expirable.expireEnd(player);
                    expirables.remove(expirable);
                    continue;
                }
                switch (min) {
                    case 1800:
                    case 900:
                    case 600:
                    case 300:
                    case 60:
                        expirable.expireMessage(player, min / 60);
                        break;
                }
            }
        } finally {
            writeUnlock();
        }
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final ExpireTimerTask _instance = new ExpireTimerTask();
    }
}
