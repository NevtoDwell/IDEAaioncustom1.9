/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.chatserver.serverpackets;

import com.ne.gs.configs.network.IPConfig;
import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.network.chatserver.ChatServerConnection;
import com.ne.gs.network.chatserver.CsServerPacket;

/**
 * @author ATracer
 */
public class SM_CS_AUTH extends CsServerPacket {

    public SM_CS_AUTH() {
        super(0x00);
    }

    @Override
    protected void writeImpl(ChatServerConnection con) {
        writeC(NetworkConfig.GAMESERVER_ID);
        writeC(IPConfig.getDefaultAddress().length);
        writeB(IPConfig.getDefaultAddress());
        writeS(NetworkConfig.CHAT_PASSWORD);
    }
}
