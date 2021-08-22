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

import com.ne.commons.network.packet.BaseServerPacket;

/**
 * @author ATracer
 */
public abstract class CsServerPacket extends BaseServerPacket {

    /**
     * constructs new server packet with specified opcode.
     *
     * @param opcode
     *     packet id
     */
    protected CsServerPacket(int opcode) {
        super(opcode);
    }

    /**
     * Write this packet data for given connection, to given buffer.
     *
     * @param con
     */
    public final void write(ChatServerConnection con, ByteBuffer buffer) {
        setBuf(buffer);
        buf.putShort((short) 0);
        buf.put((byte) getOpcode());
        writeImpl(con);
        buf.flip();
        buf.putShort((short) buf.limit());
        buf.position(0);
    }

    /**
     * Write data that this packet represents to given byte buffer.
     *
     * @param con
     */
    protected abstract void writeImpl(ChatServerConnection con);
}
