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

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.DeniedStatus;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.SystemMessageId;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.ExchangeService;
import com.ne.gs.world.World;

/**
 * @author -Avol-
 */
public class CM_EXCHANGE_REQUEST extends AionClientPacket {

    public Integer targetObjectId;

    private static final Logger log = LoggerFactory.getLogger(CM_EXCHANGE_REQUEST.class);

    @Override
    protected void readImpl() {
        targetObjectId = readD();
    }

    @Override
    protected void runImpl() {
        final Player activePlayer = getConnection().getActivePlayer();
        final Player targetPlayer = World.getInstance().findPlayer(targetObjectId);

        if (targetPlayer == null) {
            log.warn("CM_EXCHANGE_REQUEST null target from {} to {}", activePlayer.getObjectId(), targetObjectId);
            return;
        }
        /**
         * check if not trading with yourself.
         */
        if (!activePlayer.equals(targetPlayer)) {
            /**
             * check distance between players.
             */
            if (activePlayer.getKnownList().getObject(targetPlayer.getObjectId()) == null) {
                log.info("[AUDIT] Player " + activePlayer.getName() + " tried trade with player (" + targetPlayer.getName() + ") not from knownlist.");
                return;
            }
            if (!activePlayer.getRace().equals(targetPlayer.getRace())) {
                log.info("[AUDIT] Player " + activePlayer.getName() + " tried trade with player (" + targetPlayer.getName() + ") another race.");
                return;
            }
            /**
             * check if trade partner exists or is he/she a player.
             */
            if (targetPlayer != null) {
                if (targetPlayer.getPlayerSettings().isInDeniedStatus(DeniedStatus.TRADE)) {
                    sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_TRADE(targetPlayer.getName()));
                    return;
                }
                sendPacket(SM_SYSTEM_MESSAGE.STR_EXCHANGE_ASKED_EXCHANGE_TO_HIM(targetPlayer.getName()));
                RequestResponseHandler responseHandler = new RequestResponseHandler(activePlayer) {

                    @Override
                    public void acceptRequest(Creature requester, Player responder) {
                        ExchangeService.getInstance().registerExchange(activePlayer, targetPlayer);
                    }

                    @Override
                    public void denyRequest(Creature requester, Player responder) {
                        activePlayer.sendPck(new SM_SYSTEM_MESSAGE(SystemMessageId.EXCHANGE_HE_REJECTED_EXCHANGE, targetPlayer.getName()));
                    }
                };

                boolean requested = targetPlayer.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_EXCHANGE_DO_YOU_ACCEPT_EXCHANGE,
                    responseHandler);
                if (requested) {
                    targetPlayer.sendPck(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_EXCHANGE_DO_YOU_ACCEPT_EXCHANGE, 0, 0,
                        activePlayer.getName()));
                }
            }
        }
    }
}
