/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.serverpackets;

import com.ne.gs.network.loginserver.LoginServerConnection;
import com.ne.gs.network.loginserver.LsServerPacket;

/**
 * @author KID
 */
public class SM_MACBAN_CONTROL extends LsServerPacket {

    private final byte type;
    private final String address;
    private final String details;
    private final long time;

    public SM_MACBAN_CONTROL(byte type, String address, long time, String details) {
        super(10);
        this.type = type;
        this.address = address;
        this.time = time;
        this.details = details;
    }

    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeC(type);
        writeS(address);
        writeS(details);
        writeQ(time);
    }
}
