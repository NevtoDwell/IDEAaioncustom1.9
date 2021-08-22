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
 * Sends Survey HTML data to the client. This packet can be splitted over max 255 packets
 * The max length of the HTML may therefore be 255 * 65525 byte
 *
 * @author lhw and Kaipo
 */
public class SM_QUESTIONNAIRE extends AionServerPacket {

    private final int messageId;
    private final byte chunk;
    private final byte count;
    private final String html;

    public SM_QUESTIONNAIRE(int messageId, byte chunk, byte count, String html) {
        this.messageId = messageId;
        this.chunk = chunk;
        this.count = count;
        this.html = html;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(messageId);
        writeC(chunk);
        writeC(count);
        writeH(html.length() * 2);
        writeS(html);
    }
}
