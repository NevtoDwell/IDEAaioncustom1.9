/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.DeniedStatus;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_FRIEND_RESPONSE;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.SocialService;
import com.ne.gs.utils.ChatUtil;
import com.ne.gs.world.World;

/**
 * Received when a user tries to add someone as his friend
 *
 * @author Ben
 */
public class CM_FRIEND_ADD extends AionClientPacket {

    private String targetName;

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
        targetName = targetName.replace("\uE024", "");
        targetName = targetName.replace("\uE023", "");
        if (targetName.contains(ChatUtil.HEART)) {
            targetName = targetName.split(ChatUtil.HEART)[0].trim();
        }

        targetName = ChatUtil.getRealAdminName(targetName);

        final Player activePlayer = getConnection().getActivePlayer();
        final Player targetPlayer = World.getInstance().findPlayer(targetName);

        if (targetName.equalsIgnoreCase(activePlayer.getName())) {
            // Adding self to friend list not allowed - Its blocked by the client by default, so no need to send an error
        }
        // if offline
        else if (targetPlayer == null) {
            sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_OFFLINE));
        } else if (activePlayer.getFriendList().getFriend(targetPlayer.getObjectId()) != null) {
            sendPacket(new SM_FRIEND_RESPONSE(targetPlayer.getName(), SM_FRIEND_RESPONSE.TARGET_ALREADY_FRIEND));
        } else if (activePlayer.getFriendList().isFull()) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_LIST_FULL);
        } else if (activePlayer.getCommonData().getRace() != targetPlayer.getCommonData().getRace()) {
            sendPacket(new SM_FRIEND_RESPONSE(targetPlayer.getName(), SM_FRIEND_RESPONSE.TARGET_NOT_FOUND));
        } else if (targetPlayer.getFriendList().isFull()) {
            sendPacket(new SM_FRIEND_RESPONSE(targetPlayer.getName(), SM_FRIEND_RESPONSE.TARGET_LIST_FULL));
        } else if (activePlayer.getBlockList().contains(targetPlayer.getObjectId())) {
            sendPacket(new SM_FRIEND_RESPONSE(targetPlayer.getName(), SM_FRIEND_RESPONSE.TARGET_BLOCKED));
        } else if (targetPlayer.getBlockList().contains(activePlayer.getObjectId())) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_YOU_EXCLUDED(targetName));
        } else // Send request
        {
            RequestResponseHandler responseHandler = new RequestResponseHandler(activePlayer) {

                @Override
                public void acceptRequest(Creature requester, Player responder) {
                    if (!targetPlayer.getCommonData().isOnline()) {
                        sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_OFFLINE));
                    } else if (activePlayer.getFriendList().isFull() || responder.getFriendList().isFull()) {
                        return;
                    } else {
                        SocialService.makeFriends((Player) requester, responder);
                    }

                }

                @Override
                public void denyRequest(Creature requester, Player responder) {
                    sendPacket(new SM_FRIEND_RESPONSE(targetName, SM_FRIEND_RESPONSE.TARGET_DENIED));

                }
            };

            boolean requested = targetPlayer.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_BUDDYLIST_ADD_BUDDY_REQUEST, responseHandler);
            // If the player is busy and could not be asked
            if (!requested) {
                sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_BUSY);
            } else {
                if (targetPlayer.getPlayerSettings().isInDeniedStatus(DeniedStatus.FRIEND)) {
                    sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_FRIEND(targetPlayer.getName()));
                    return;
                }
                // Send question packet to buddy
                targetPlayer.getClientConnection().sendPacket(
                    new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_BUDDYLIST_ADD_BUDDY_REQUEST, activePlayer.getObjectId(), 0, activePlayer.getName()));
            }
        }
    }

}
