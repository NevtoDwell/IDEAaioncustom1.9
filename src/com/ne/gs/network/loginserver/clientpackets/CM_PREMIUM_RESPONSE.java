/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.clientpackets;

import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.network.loginserver.LsClientPacket;

/**
 * @author KID
 */
public class CM_PREMIUM_RESPONSE extends LsClientPacket {

    private int requestId;
    private int result;
    private long points;

    public CM_PREMIUM_RESPONSE(int opCode) {
        super(opCode);
    }

    @Override
    protected void readImpl() {
        requestId = readD();
        result = readD();
        points = readQ();
    }

    @Override
    protected void runImpl() {
        InGameShopEn.getInstance().finishRequest(requestId, result, points);
    }
}
