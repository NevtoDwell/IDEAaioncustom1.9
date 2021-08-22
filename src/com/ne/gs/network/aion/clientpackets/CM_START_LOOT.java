/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.drop.DropService;

/**
 * @author alexa026, Correted by Metos, ATracer
 */
public class CM_START_LOOT extends AionClientPacket {

    /**
     * Target object id that client wants to TALK WITH or 0 if wants to unselect
     */

    private int targetObjectId;
    private int action;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        targetObjectId = readD();// empty
        action = readC();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (action == 0) {
            DropService.getInstance().requestDropList(player, targetObjectId);
            player.getObserveController().notifyObservers(com.ne.gs.controllers.observer.ObserverType.LOOT);
        } else if (action == 1) {
            DropService.getInstance().closeDropList(player, targetObjectId);
        }
    }
}
