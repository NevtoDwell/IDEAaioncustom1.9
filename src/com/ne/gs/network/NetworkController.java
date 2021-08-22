/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network;

/**
 * @author KID
 */
public class NetworkController {

    private static final NetworkController instance = new NetworkController();

    public static NetworkController getInstance() {
        return instance;
    }

    private byte serverCount = 1;

    public final byte getServerCount() {
        return serverCount;
    }

    public final void setServerCount(byte count) {
        serverCount = count;
    }
}
