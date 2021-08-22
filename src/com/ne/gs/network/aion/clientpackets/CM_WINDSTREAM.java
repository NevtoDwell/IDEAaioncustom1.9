/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.actions.PlayerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.WindstreamAction;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_WINDSTREAM;

public class CM_WINDSTREAM extends AionClientPacket {

    private final Logger log = LoggerFactory.getLogger(CM_WINDSTREAM.class);
    int teleportId;
    int distance;
    int actionId;
    WindstreamAction action;

    @Override
    protected void readImpl() {
        teleportId = readD(); // typical teleport id (ex : 94001 for talloc hallow in inggison)
        distance = readD(); // 600 for talloc.
        actionId = readD();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        this.action = WindstreamAction.getById(actionId);
        if (action == null) {
            log.error("Unknown Windstream action #" + actionId + " was found! Player: " + player.getName());
            return;
        }
        switch (this.action) {
            case ENTER:
                player.unsetPlayerMode(PlayerMode.RIDE);
                player.getWindstreamControllder().enterWindstream(teleportId, distance);
                break;
            case START:
                player.getWindstreamControllder().startWindstream(teleportId, distance);
                break;
            case NORMAL_END:
            case INTERRUPT:
                player.getWindstreamControllder().exitWindstream(action.getId());
                break;
            case INTERRUPT_RELATED:
                player.sendPck(new SM_WINDSTREAM(action.getId(), 1));
                break;
            case BOOST_START:
                player.getWindstreamControllder().startBoost();
                break;
            case BOOST_END:
                player.getWindstreamControllder().endBoost();
                break;
            default:
                log.error("Unknown Windstream state #" + actionId + " was found!");
        }
    }
}
