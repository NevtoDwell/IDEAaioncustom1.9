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
import com.ne.gs.model.ingameshop.IGRequest;
import com.ne.gs.network.loginserver.LoginServerConnection;
import com.ne.gs.network.loginserver.LsServerPacket;

/**
 * @author KID
 */
public class SM_PREMIUM_CONTROL extends LsServerPacket {

    private final IGRequest request;
    private final long cost;

    public SM_PREMIUM_CONTROL(IGRequest request, long cost) {
        super(0x0B);
        this.request = request;
        this.cost = cost;
    }

    @Override
    protected void writeImpl(LoginServerConnection con) {
        writeD(request.accountId);
        writeD(request.requestId);
        writeQ(cost);
        writeC(NetworkConfig.GAMESERVER_ID);
    }
}
