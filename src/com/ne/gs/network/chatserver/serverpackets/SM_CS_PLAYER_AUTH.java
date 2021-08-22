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
public class SM_CS_PLAYER_AUTH extends CsServerPacket {

    private final int playerId;
    private final String playerLogin;
    private final String nick;

    public SM_CS_PLAYER_AUTH(int playerId, String playerLogin, String nick) {
        super(0x01);
        this.playerId = playerId;
        this.playerLogin = playerLogin;
        this.nick = nick;
    }

    @Override
    protected void writeImpl(ChatServerConnection con) {
        writeD(playerId);
        writeS(playerLogin);
        writeS(nick);
    }
}
