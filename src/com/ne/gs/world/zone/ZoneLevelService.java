/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.zone;

import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;

/**
 * @author ATracer
 */
public final class ZoneLevelService {

    private static final long DROWN_PERIOD = 2000;

    /**
     * Check water level (start drowning) and map death level (die)
     */
    public static void checkZoneLevels(Player player) {
        World world = World.getInstance();
        float z = player.getZ();

        if (player.getLifeStats().isAlreadyDead()) {
            return;
        }

        if (z < world.getWorldMap(player.getWorldId()).getDeathLevel()) {
            player.getController().die();
            return;
        }

        // TODO need fix character height
        float playerheight = player.getPlayerAppearance().getHeight() * 1.6f;
        if (z < world.getWorldMap(player.getWorldId()).getWaterLevel() - playerheight) {
            startDrowning(player);
        } else {
            stopDrowning(player);
        }
    }

    /**
     * @param player
     */
    private static void startDrowning(Player player) {
        if (!isDrowning(player)) {
            scheduleDrowningTask(player);
        }
    }

    /**
     * @param player
     */
    private static void stopDrowning(Player player) {
        if (isDrowning(player)) {
            player.getController().cancelTask(TaskId.DROWN);
        }

    }

    /**
     * @param player
     *
     * @return
     */
    private static boolean isDrowning(Player player) {
        return player.getController().getTask(TaskId.DROWN) == null ? false : true;
    }

    /**
     * @param player
     */
    private static void scheduleDrowningTask(final Player player) {
        player.getController().addTask(TaskId.DROWN, ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                int value = Math.round(player.getLifeStats().getMaxHp() / 10);
                // TODO retail emotion, attack_status packets sending
                if (!player.getLifeStats().isAlreadyDead()) {
                    if (!player.isInvul()) {
                        player.getLifeStats().reduceHp(value, player);
                        player.getLifeStats().sendHpPacketUpdate();
                    }
                } else {
                    stopDrowning(player);
                }
            }
        }, 0, DROWN_PERIOD));
    }
}
