/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.BlockedPlayer;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.SocialService;

/**
 * @author Ben
 */
public class CM_BLOCK_SET_REASON extends AionClientPacket {

    String targetName;
    String reason;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        targetName = readS();
        reason = readS();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();
        BlockedPlayer target = activePlayer.getBlockList().getBlockedPlayer(targetName);

        if (target == null) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_NOT_IN_LIST);
        } else {
            SocialService.setBlockedReason(activePlayer, target, reason);
        }
    }
}
