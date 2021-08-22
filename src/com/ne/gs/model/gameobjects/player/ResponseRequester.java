/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the asking of and responding to <tt>SM_QUESTION_WINDOW</tt>
 *
 * @author Ben
 */
public class ResponseRequester {

    private static final Logger log = LoggerFactory.getLogger(ResponseRequester.class);

    private final Player player;
    private final HashMap<Integer, RequestResponseHandler> map = new HashMap<>();

    public ResponseRequester(Player player) {
        this.player = player;
    }

    /**
     * Adds this handler to this messageID, returns false if there already exists one
     *
     * @param messageId
     *     ID of the request message
     *
     * @return true or false
     */
    public synchronized boolean putRequest(int messageId, RequestResponseHandler handler) {
        if (map.containsKey(messageId)) {
            return false;
        }

        map.put(messageId, handler);
        return true;
    }

    /**
     * Responds to the given message ID with the given response Returns success
     *
     * @param messageId
     * @param response
     *
     * @return Success
     */
    public synchronized boolean respond(int messageId, int response) {
        RequestResponseHandler handler = map.remove(messageId);
        if (handler != null) {
            log.debug("RequestResponseHandler triggered for response code " + messageId + " from " + player.getName());
            handler.handle(player, response);
            return true;
        }
        return false;
    }

    /**
     * Automatically responds 0 to all requests, passing the given player as the responder
     */
    public synchronized void denyAll() {
        for (RequestResponseHandler handler : map.values()) {
            handler.handle(player, 0);
        }

        map.clear();
    }
}
