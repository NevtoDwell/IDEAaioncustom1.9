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
 * @author LokiReborn
 */
public class SM_WINDSTREAM_ANNOUNCE extends AionServerPacket {

    private final int bidirectional;
    private final int mapId;
    private final int streamId;
    private final int boost;

    public SM_WINDSTREAM_ANNOUNCE(int bidirectional, int mapId, int streamId, int boost) {
        this.bidirectional = bidirectional;
        this.mapId = mapId;
        this.streamId = streamId;
        this.boost = boost;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(bidirectional);
        writeD(mapId);
        writeD(streamId);
        writeC(boost);
    }
}
