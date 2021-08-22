/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.serverpackets;

import java.util.List;

import com.ne.commons.network.IPRange;
import com.ne.gs.configs.network.IPConfig;
import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.network.loginserver.LoginServerConnection;
import com.ne.gs.network.loginserver.LsServerPacket;

/**
 * This is authentication packet that gs will send to login server for registration.
 *
 * @author -Nemesiss-
 */
public class SM_GS_AUTH extends LsServerPacket {

    public SM_GS_AUTH() {
        super(0x00);
    }

    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeC(NetworkConfig.GAMESERVER_ID);
        writeC(IPConfig.getDefaultAddress().length);
        writeB(IPConfig.getDefaultAddress());

        List<IPRange> ranges = IPConfig.getRanges();
        int size = ranges.size();
        writeD(size);
        for (int i = 0; i < size; i++) {
            IPRange ipRange = ranges.get(i);
            byte[] min = ipRange.getMinAsByteArray();
            byte[] max = ipRange.getMaxAsByteArray();
            writeC(min.length);
            writeB(min);
            writeC(max.length);
            writeB(max);
            writeC(ipRange.getAddress().length);
            writeB(ipRange.getAddress());
        }

        writeH(NetworkConfig.GAME_PORT);
        writeD(NetworkConfig.MAX_ONLINE_PLAYERS);
        writeS(NetworkConfig.LOGIN_PASSWORD);
    }
}
