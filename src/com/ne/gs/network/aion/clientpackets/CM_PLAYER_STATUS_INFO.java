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
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.common.events.TeamCommand;
import com.ne.gs.model.team2.common.service.PlayerTeamCommandService;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * Called when entering the world and during group management
 *
 * @author Lyahim, ATracer, Simple, xTz
 */

public class CM_PLAYER_STATUS_INFO extends AionClientPacket {

    private int commandCode;
    private int playerObjId;
    private int allianceGroupId;
    private int secondObjectId;

    @Override
    protected void readImpl() {
        commandCode = readC();
        playerObjId = readD();
        allianceGroupId = readD();
        secondObjectId = readD();

    }

    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();
        TeamCommand command = TeamCommand.getCommand(commandCode);
        switch (command) {
            case GROUP_SET_LFG:
                activePlayer.setLookingForGroup(playerObjId == 2);
                break;
            case ALLIANCE_CHANGE_GROUP:
                PlayerAllianceService.changeMemberGroup(activePlayer, playerObjId, secondObjectId, allianceGroupId);
                break;
            default:
                PlayerTeamCommandService.executeCommand(activePlayer, command, playerObjId);
        }
    }
}
