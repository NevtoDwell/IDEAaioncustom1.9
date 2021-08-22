/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.DeniedStatus;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.model.team2.league.LeagueService;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.ChatUtil;
import com.ne.gs.world.World;

/**
 * @author Lyahim, ATracer Modified by Simple
 */
public class CM_INVITE_TO_GROUP extends AionClientPacket {

    private String name;
    private int inviteType;

    @Override
    protected void readImpl() {
        inviteType = readC();
        name = readS();
    }

    @Override
    protected void runImpl() {
        String playerName = ChatUtil.undecorateName(name);

        Player inviter = getConnection().getActivePlayer();
        if (inviter.getLifeStats().isAlreadyDead()) {
            // You cannot issue an invitation while you are dead.
            inviter.sendPck(new SM_SYSTEM_MESSAGE(1300163));
            return;
        }

        Player invited = World.getInstance().findPlayer(playerName);
        if (invited != null) {
            if (invited.getPlayerSettings().isInDeniedStatus(DeniedStatus.GROUP)) {
                sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_INVITE_PARTY(invited.getName()));
                return;
            }
            switch (inviteType) {
                case 0:
                    PlayerGroupService.inviteToGroup(inviter, invited);
                    break;
                case 12: // 2.5
                    PlayerAllianceService.inviteToAlliance(inviter, invited);
                    break;
                case 28:
                    LeagueService.inviteToLeague(inviter, invited);
                    break;
                default:
                    inviter.sendMsg("You used an unknown invite type: " + inviteType);
                    break;
            }
        } else {
            inviter.getClientConnection().sendPacket(SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(name));
        }
    }
}
