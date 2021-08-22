/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.player.Friend;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * Sent to update a player's status in a friendlist
 *
 * @author Ben
 */
public class SM_FRIEND_UPDATE extends AionServerPacket {

    private final int friendObjId;

    private static final Logger log = LoggerFactory.getLogger(SM_FRIEND_UPDATE.class);

    public SM_FRIEND_UPDATE(int friendObjId) {
        this.friendObjId = friendObjId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        Friend f = con.getActivePlayer().getFriendList().getFriend(friendObjId);
        if (f == null) {
            log.debug("Attempted to update friend list status of " + friendObjId + " for " + con.getActivePlayer().getName()
                + " - object ID not found on friend list");
        } else {
            writeS(f.getName());
            writeD(f.getLevel());
            writeD(f.getPlayerClass().getClassId());
            writeC(f.isOnline() ? 1 : 0); // Online status - No idea why this and f.getStatus are used
            writeD(f.getMapId());
            writeD(f.getLastOnlineTime()); // Date friend was last online as a Unix timestamp.
            writeS(f.getNote());
            writeC(f.getStatus().getId());
        }
    }
}
