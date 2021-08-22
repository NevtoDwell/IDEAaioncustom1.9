/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.ingameshop;

/**
 * @author KID
 */
public class IGRequest {

    public boolean gift = false, sync = false;
    public int playerId;
    public int cost;
    public int requestId, itemObjId;
    public String receiver, message;
    public int accountId;

    public IGRequest(int requestId, int playerId, int itemObjId) {
        this.requestId = requestId;
        this.playerId = playerId;
        this.itemObjId = itemObjId;
    }

    public IGRequest(int requestId, int playerId, String receiver, String message, int itemObjId) {
        this.requestId = requestId;
        this.playerId = playerId;
        this.receiver = receiver;
        this.message = message;
        this.itemObjId = itemObjId;
        gift = true;
    }

    public IGRequest(int requestId, int playerId, int cost, boolean a) {
        this.requestId = requestId;
        this.playerId = playerId;
        this.cost = cost;
        sync = a;
    }
}
