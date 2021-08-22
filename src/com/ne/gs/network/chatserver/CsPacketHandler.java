/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.chatserver;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.network.chatserver.ChatServerConnection.State;

/**
 * @author ATracer
 */
public class CsPacketHandler {

    /**
     * logger for this class
     */
    private static final Logger log = LoggerFactory.getLogger(CsPacketHandler.class);

    private final Map<State, Map<Integer, CsClientPacket>> packetPrototypes = new HashMap<>();

    /**
     * Reads one packet from given ByteBuffer
     *
     * @param data
     * @param client
     *
     * @return GsClientPacket object from binary data
     */
    public CsClientPacket handle(ByteBuffer data, ChatServerConnection client) {
        State state = client.getState();
        int id = data.get() & 0xff;

        return getPacket(state, id, data, client);
    }

    /**
     * @param packetPrototype
     * @param states
     */
    public void addPacketPrototype(CsClientPacket packetPrototype, State... states) {
        for (State state : states) {
            Map<Integer, CsClientPacket> pm = packetPrototypes.get(state);
            if (pm == null) {
                pm = new HashMap<>();
                packetPrototypes.put(state, pm);
            }
            pm.put(packetPrototype.getOpcode(), packetPrototype);
        }
    }

    /**
     * @param state
     * @param id
     * @param buf
     * @param con
     *
     * @return
     */
    private CsClientPacket getPacket(State state, int id, ByteBuffer buf, ChatServerConnection con) {
        CsClientPacket prototype = null;

        Map<Integer, CsClientPacket> pm = packetPrototypes.get(state);
        if (pm != null) {
            prototype = pm.get(id);
        }

        if (prototype == null) {
            unknownPacket(state, id);
            return null;
        }

        CsClientPacket res = prototype.clonePacket();
        res.setBuffer(buf);
        res.setConnection(con);

        return res;
    }

    /**
     * Logs unknown packet.
     *
     * @param state
     * @param id
     */
    private void unknownPacket(State state, int id) {
        log.warn(String.format("Unknown packet recived from Chat Server: 0x%02X state=%s", id, state.toString()));
    }
}
