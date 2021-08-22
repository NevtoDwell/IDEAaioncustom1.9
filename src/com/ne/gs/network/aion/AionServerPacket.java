/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion;

import java.nio.ByteBuffer;

import com.ne.commons.network.packet.BaseServerPacket;
import com.ne.gs.network.Crypt;

/**
 * Base class for every GS -> Aion Server Packet.
 *
 * @author -Nemesiss-
 */
public abstract class AionServerPacket extends BaseServerPacket {

    /**
     * Write packet opcodec and two additional bytes
     *
     * @param value
     */
    private void writeOP(int value) {
        /** obfuscate packet id */
        int op = Crypt.encodeOpcodec(value);
        buf.putShort((short) (op));
        /** put static server packet code */
        buf.put(Crypt.staticServerPacketCode);

        /** for checksum? */
        buf.putShort((short) (~op));
    }

    public final void write(AionConnection con) {
        write(con, buf);
    }

    /**
     * Write and encrypt this packet data for given connection, to given buffer.
     *
     * @param con
     */
    public final void write(AionConnection con, ByteBuffer buffer) {
        setBuf(buffer);
        buf.putShort((short) 0);
        writeOP(getOpcode());
        writeImpl(con);
        buf.flip();
        buf.putShort((short) buf.limit());
        ByteBuffer b = buf.slice();
        buf.position(0);
        con.encrypt(b);
    }

    /**
     * Write data that this packet represents to given byte buffer.
     *
     * @param con
     */
    protected void writeImpl(AionConnection con) {

    }

    public final ByteBuffer getBuf() {
        return buf;
    }

    /**
     * Write String to buffer
     *
     * @param text
     * @param size
     */
    protected final void writeS(String text, int size) {
        if (text == null) {
            buf.put(new byte[size]);
        } else {
            int len = text.length();
            for (int i = 0; i < len; i++) {
                buf.putChar(text.charAt(i));
            }
            buf.put(new byte[size - (len * 2)]);
        }
    }

    protected void writeNameId(int nameId) {
        writeH(0x24);
        writeD(nameId);
        writeH(0x00);
    }
}
