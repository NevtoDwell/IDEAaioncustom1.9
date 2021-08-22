/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.factories;

import com.ne.gs.network.chatserver.ChatServerConnection.State;
import com.ne.gs.network.chatserver.CsClientPacket;
import com.ne.gs.network.chatserver.CsPacketHandler;
import com.ne.gs.network.chatserver.clientpackets.CM_CS_AUTH_RESPONSE;
import com.ne.gs.network.chatserver.clientpackets.CM_CS_CHAT_MESSAGE;
import com.ne.gs.network.chatserver.clientpackets.CM_CS_PLAYER_AUTH_RESPONSE;

/**
 * @author ATracer
 */
public class CsPacketHandlerFactory {

    private final CsPacketHandler handler = new CsPacketHandler();

    /**
     */
    public CsPacketHandlerFactory() {
        addPacket(new CM_CS_AUTH_RESPONSE(0x00), State.CONNECTED);
        addPacket(new CM_CS_PLAYER_AUTH_RESPONSE(0x01), State.AUTHED);
        addPacket(new CM_CS_CHAT_MESSAGE(0x02), State.AUTHED);
    }

    /**
     * @param prototype
     * @param states
     */
    private void addPacket(CsClientPacket prototype, State... states) {
        handler.addPacketPrototype(prototype, states);
    }

    /**
     * @return handler
     */
    public CsPacketHandler getPacketHandler() {
        return handler;
    }
}
