/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;

/**
 * @author MrPoke
 */
public class CM_PLAY_MOVIE_END extends AionClientPacket {

    @SuppressWarnings("unused")
    private int type;
    private int movieId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        type = readC();
        readD();
        readD();
        movieId = readH();
        readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        QuestEngine.getInstance().onMovieEnd(new QuestEnv(null, player, 0, 0), movieId);
        player.getPosition().getWorldMapInstance().getInstanceHandler().onPlayMovieEnd(player, movieId);
    }

}
