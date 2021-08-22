/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import com.ne.gs.model.gameobjects.Creature;

/**
 * Implemented by handlers of <tt>CM_QUESTION_RESPONSE</tt> responses
 *
 * @author Ben, hex1r0
 * @modified Lyahim
 */
public abstract class RequestResponseHandler<T extends Creature> {

    private final T requester;

    public RequestResponseHandler(T requester) {
        this.requester = requester;
    }

    /**
     * Called when a response is received
     *
     * @param responder
     *     Player whom responded to this request
     * @param response
     *     The response the player gave, usually 0 = no 1 = yes
     */
    public void handle(Player responder, int response) {
        if (response == 0) {
            denyRequest(requester, responder);
        } else {
            acceptRequest(requester, responder);
        }
    }

    /**
     * Called when the player accepts a request
     *
     * @param requester
     *     Creature whom requested this response
     * @param responder
     *     Player whom responded to this request
     */
    public abstract void acceptRequest(T requester, Player responder);

    /**
     * Called when the player denies a request
     *
     * @param requester
     *     Creature whom requested this response
     * @param responder
     *     Player whom responded to this request
     */
    public abstract void denyRequest(T requester, Player responder);

}
