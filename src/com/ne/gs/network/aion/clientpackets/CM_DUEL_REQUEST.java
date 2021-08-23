/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.player.DeniedStatus;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.DuelService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.zone.ZoneName;

/**
 * @author xavier
 */
public class CM_DUEL_REQUEST extends AionClientPacket {

    /**
     * Target object id that client wants to start duel with
     */
    private int objectId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        objectId = readD();
    }

    @Override
    protected void runImpl() {
    Player activePlayer = getConnection().getActivePlayer();
    AionObject target = activePlayer.getKnownList().getObject(objectId);

    if (target == null || activePlayer.getLifeStats().isAlreadyDead()
	    || activePlayer.getPosition().getWorldMapInstance().getInstanceHandler().isDuelDisabled()) {
        return;
    }

    //activePlayer - кто кидает пакет дуэли!
    //targetPlayer - тот кто получает пакет дуэли!

    if (target instanceof Player && !target.equals(activePlayer)) {
        DuelService duelService = DuelService.getInstance();

        Player targetPlayer = (Player) target;

        if (duelService.isDueling(activePlayer.getObjectId())) {
	        sendPacket(SM_SYSTEM_MESSAGE.STR_DUEL_YOU_ARE_IN_DUEL_ALREADY);
	        return;
        }
        if (duelService.isDueling(targetPlayer.getObjectId())) {
	        sendPacket(SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_IN_DUEL_ALREADY(target.getName()));
	        return;
        }
        if (targetPlayer.getPlayerSettings().isInDeniedStatus(DeniedStatus.DUEL)) {
	        sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_DUEL(targetPlayer.getName()));
	        return;
        }
        if (targetPlayer.getLifeStats().isAlreadyDead()) {
	        sendPacket(SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_INVALID(target.getName()));
	        return;
        }
        if (targetPlayer.isTrading()) {
            PacketSendUtility.sendYellowMessageOnCenter(activePlayer, "Дуэль отклонена: \"" + targetPlayer.getName() + "\" находится в торговле!" +
                    " | Duel rejected: \"" + targetPlayer.getName() + "\" is in trade!");
            return;
        }
        if (activePlayer.isTrading()) {
            PacketSendUtility.sendYellowMessageOnCenter(activePlayer, "Дуэль отклонена: Вы находитесь в торговле!" +
                    " | Duel rejected: You are in trade!");
            return;
        }
        if (targetPlayer.getResponseRequester().respond(50028)) {
            PacketSendUtility.sendYellowMessageOnCenter(activePlayer,  "Персонаж \"" + target.getName() + "\" уже вызван на дуэль! | \"" +
                    target.getName() + "\" has already been invited to a duel!");
            return;
        }

        if (targetPlayer.isInsideZone(ZoneName.get("LC1_PVP_SUB_C")) || activePlayer.isInsideZone(ZoneName.get("DC1_PVP_ZONE"))) {
            PacketSendUtility.sendPck(activePlayer, SM_SYSTEM_MESSAGE.STR_MSG_DUEL_CANT_IN_THIS_ZONE);
            return;
        }

        duelService.confirmDuelWith(activePlayer, targetPlayer);
        } else {
            sendPacket(SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_INVALID(target.getName()));
        }
    }
}
