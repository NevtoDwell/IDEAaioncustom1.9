/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.chatserver.serverpackets;

import com.ne.gs.network.chatserver.ChatServerConnection;
import com.ne.gs.network.chatserver.CsServerPacket;

/**
 * @author ATracer
 */
public class SM_CS_PLAYER_LOGOUT extends CsServerPacket {

    private final int playerId;

    public SM_CS_PLAYER_LOGOUT(int playerId) {
        super(0x02);
        this.playerId = playerId;
    }

    @Override
    protected void writeImpl(ChatServerConnection con) {
        writeD(playerId);
    }
}
