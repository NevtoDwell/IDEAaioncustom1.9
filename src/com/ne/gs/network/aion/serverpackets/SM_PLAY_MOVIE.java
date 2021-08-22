/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author -orz-, MrPoke
 */
public class SM_PLAY_MOVIE extends AionServerPacket {

    private int type = 1; // if 1: CutSceneMovies else CutScenes
    private int movieId = 0;
    private int id = 0; // id scene ?
    private int restrictionId;
    private int objectId;

    public SM_PLAY_MOVIE(int type, int movieId) {
        this.type = type;
        this.movieId = movieId;
    }

    public SM_PLAY_MOVIE(int type, int id, int movieId, int restrictionId) {
        this(type, movieId);
        this.id = id;
        this.restrictionId = restrictionId;
    }

    public SM_PLAY_MOVIE(int type, int id, int movieId, int restrictionId, int objectId) {
        this(type, id, movieId, restrictionId);
        this.objectId = objectId;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(type);
        writeD(objectId);
        writeD(id);
        writeH(movieId);
        writeD(restrictionId);
    }
}
