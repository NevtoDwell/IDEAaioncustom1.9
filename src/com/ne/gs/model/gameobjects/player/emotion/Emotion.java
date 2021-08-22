/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player.emotion;

import com.ne.gs.model.IExpirable;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */
public class Emotion implements IExpirable {

    private final int id;
    private final int dispearTime;

    /**
     * @param id
     * @param dispearTime
     */
    public Emotion(int id, int dispearTime) {
        this.id = id;
        this.dispearTime = dispearTime;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    public int getRemainingTime() {
        if (dispearTime == 0) {
            return 0;
        }
        return dispearTime - (int) (System.currentTimeMillis() / 1000);
    }

    @Override
    public int getExpireTime() {
        return dispearTime;
    }

    @Override
    public void expireEnd(Player player) {
        player.getEmotions().remove(id);

    }

    @Override
    public void expireMessage(Player player, int time) {
    }

    @Override
    public boolean canExpireNow() {
        return true;
    }
}
