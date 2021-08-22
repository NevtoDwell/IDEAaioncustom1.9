/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.serverpackets;

import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.network.loginserver.LoginServerConnection;
import com.ne.gs.network.loginserver.LsServerPacket;

/**
 * @author KID
 */
public class SM_LS_PONG extends LsServerPacket {

    private final int pid;

    public SM_LS_PONG(int pid) {
        super(12);
        this.pid = pid;
    }

    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeC(NetworkConfig.GAMESERVER_ID);
        writeD(pid);
    }
}
