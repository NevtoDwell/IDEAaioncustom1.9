/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.player.FriendList.Status;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_ONLINE_STATUS;

/**
 * Packet received when a user changes his buddylist status
 *
 * @author Ben
 */
public class CM_FRIEND_STATUS extends AionClientPacket {

    private final Logger log = LoggerFactory.getLogger(CM_FRIEND_STATUS.class);
    // The users new status
    private byte status;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        status = (byte) readC();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();
        activePlayer.sendPck(new SM_ONLINE_STATUS(status));
        Status statusEnum = Status.getByValue(status);
        if (statusEnum == null) {
            log.warn("received unknown status id " + status);
            statusEnum = Status.ONLINE;
        }
        activePlayer.getFriendList().setStatus(statusEnum);
    }
}
