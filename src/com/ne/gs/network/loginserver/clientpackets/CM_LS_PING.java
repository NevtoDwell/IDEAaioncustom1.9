/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.clientpackets;

import java.lang.management.ManagementFactory;

import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.network.loginserver.LsClientPacket;
import com.ne.gs.network.loginserver.serverpackets.SM_LS_PONG;

/**
 * @author KID
 */
public class CM_LS_PING extends LsClientPacket {

    private static final int PID;

    static {
        int pid = -1;
        try {
            pid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        } catch (Exception ex) {
        }

        PID = pid;
    }

    public CM_LS_PING(int opCode) {
        super(opCode);
    }

    @Override
    protected void readImpl() {
        // trigger
    }

    @Override
    protected void runImpl() {
        LoginServer.getInstance().sendPacket(new SM_LS_PONG(PID));
    }
}
