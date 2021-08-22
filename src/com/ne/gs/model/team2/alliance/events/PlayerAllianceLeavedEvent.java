/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance.events;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceMember;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.common.events.PlayerLeavedEvent;
import com.ne.gs.model.team2.common.legacy.PlayerAllianceEvent;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_LEAVE_GROUP_MEMBER;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class PlayerAllianceLeavedEvent extends PlayerLeavedEvent<PlayerAllianceMember, PlayerAlliance> {

    public PlayerAllianceLeavedEvent(PlayerAlliance alliance, Player player) {
        super(alliance, player);
    }

    public PlayerAllianceLeavedEvent(PlayerAlliance team, Player player, PlayerLeavedEvent.LeaveReson reason, String banPersonName) {
        super(team, player, reason, banPersonName);
    }

    public PlayerAllianceLeavedEvent(PlayerAlliance alliance, Player player, PlayerLeavedEvent.LeaveReson reason) {
        super(alliance, player, reason);
    }

    @Override
    public void handleEvent() {
        team.removeMember(leavedPlayer.getObjectId());
        team.getViceCaptainIds().remove(leavedPlayer.getObjectId());

        if (leavedPlayer.isOnline()) {
            leavedPlayer.sendPck(new SM_LEAVE_GROUP_MEMBER());
        }

        team.apply(this);

        switch (reason) {
            case BAN:
            case LEAVE:
            case LEAVE_TIMEOUT:
                if (team.onlineMembers() <= 1) {
                    PlayerAllianceService.disband(team);
                } else if (leavedPlayer.equals(team.getLeader().getObject())) {
                    team.onEvent(new ChangeAllianceLeaderEvent(team));
                }
                if (reason == LeaveReson.BAN) {
                    leavedPlayer.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_BAN_ME(banPersonName));
                }

                break;
            case DISBAND:
                leavedPlayer.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_DISPERSED);
                break;
        }

        if (leavedPlayer.isInInstance()) {
            ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (!leavedPlayer.isInAlliance2() && leavedPlayer.getPosition().getWorldMapInstance().getRegistredAlliance() != null) {
                        InstanceService.moveToExitPoint(leavedPlayer);
                    }
                }
            }, 10000);
        }
    }

    @Override
    public boolean apply(PlayerAllianceMember member) {
        Player player = member.getObject();

        player.sendPck(new SM_ALLIANCE_MEMBER_INFO(leavedTeamMember, PlayerAllianceEvent.LEAVE));
        player.sendPck(new SM_ALLIANCE_INFO(team));

        switch (reason) {
            case LEAVE_TIMEOUT:
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_HE_LEAVED_PARTY(leavedPlayer.getName()));
                break;
            case LEAVE:
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_HE_LEAVED_PARTY(leavedPlayer.getName()));
                break;
            case DISBAND:
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_DISPERSED);
                break;
            case BAN:
                player.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_BAN_HIM(banPersonName, leavedPlayer.getName()));
                break;
        }

        return true;
    }

}
