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

import com.ne.gs.model.gameobjects.player.Friend;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.SocialService;

/**
 * @author Ben
 */
public class CM_FRIEND_DEL extends AionClientPacket {

    private String targetName;
    private static final Logger log = LoggerFactory.getLogger(CM_FRIEND_DEL.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        targetName = readS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {

        Player activePlayer = getConnection().getActivePlayer();
        Friend target = activePlayer.getFriendList().getFriend(targetName);
        if (target == null) {
            log.warn(activePlayer.getName() + " tried to delete friend " + targetName + " who is not his friend");
            sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_NOT_IN_LIST);
        } else {
            SocialService.deleteFriend(activePlayer, target.getOid());
        }
    }
}
