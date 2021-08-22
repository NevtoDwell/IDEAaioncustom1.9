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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_BLOCK_RESPONSE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.SocialService;
import com.ne.gs.world.World;

/**
 * @author Ben
 */
public class CM_BLOCK_ADD extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_BLOCK_ADD.class);

    private String targetName;
    private String reason;

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

        Player targetPlayer = World.getInstance().findPlayer(targetName);

        // Trying to block self
        if (activePlayer.getName().equalsIgnoreCase(targetName)) {
            sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.CANT_BLOCK_SELF, targetName));
        } else if (activePlayer.getBlockList().isFull()) {
            sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.LIST_FULL, targetName));
        } else if (targetPlayer == null) {
            //sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.TARGET_NOT_FOUND, targetName));
            activePlayer.sendMsg("Игрок " + targetName + " не найден (Не в сети, либо не существует).");
        } else if (activePlayer.getFriendList().getFriend(targetPlayer.getObjectId()) != null) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_NO_BUDDY);
        } else if (activePlayer.getBlockList().contains(targetPlayer.getObjectId())) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_ALREADY_BLOCKED);
        } else if (!SocialService.addBlockedUser(activePlayer, targetPlayer, reason)) {
            log.error("Failed to add " + targetPlayer.getName() + " to the block list for " + activePlayer.getName() + " - check database setup.");
        }

    }

}
