/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.emotion.Emotion;

/**
 * @author Mr. Poke
 */
public abstract class PlayerEmotionListDAO implements DAO {

    /*
     * (non-Javadoc)
     * @see com.ne.commons.database.dao.DAO#getClassName()
     */
    @Override
    public String getClassName() {
        return PlayerEmotionListDAO.class.getName();
    }

    /**
     * @param player
     */
    public abstract void loadEmotions(Player player);

    /**
     * @param player
     */
    public abstract void insertEmotion(Player player, Emotion emotion);

    public abstract void deleteEmotion(int playerId, int emotionId);
}
