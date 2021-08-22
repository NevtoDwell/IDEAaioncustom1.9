/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group.events;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.common.events.PlayerLeavedEvent;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupMember;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_LEAVE_GROUP_MEMBER;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class PlayerGroupLeavedEvent extends PlayerLeavedEvent<PlayerGroupMember, PlayerGroup> {

    public PlayerGroupLeavedEvent(PlayerGroup alliance, Player player) {
        super(alliance, player);
    }

    public PlayerGroupLeavedEvent(PlayerGroup team, Player player, PlayerLeavedEvent.LeaveReson reason, String banPersonName) {
        super(team, player, reason, banPersonName);
    }

    public PlayerGroupLeavedEvent(PlayerGroup alliance, Player player, PlayerLeavedEvent.LeaveReson reason) {
        super(alliance, player, reason);
    }

    @Override
    public void handleEvent() {
        team.removeMember(leavedPlayer.getObjectId());

        if (leavedPlayer.isMentor()) {
            team.onEvent(new PlayerGroupStopMentoringEvent(team, leavedPlayer));
        }

        team.apply(this);

        leavedPlayer.sendPck(new SM_LEAVE_GROUP_MEMBER());
        switch (reason) {
            case BAN:
            case LEAVE:
                // PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_SECEDE); // client side?
                if (team.onlineMembers() <= 1) {
                    PlayerGroupService.disband(team);
                } else if (leavedPlayer.equals(team.getLeader().getObject())) {
                    team.onEvent(new ChangeGroupLeaderEvent(team));
                }
                if (reason == LeaveReson.BAN) {
                    leavedPlayer.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_YOU_ARE_BANISHED);
                }
                break;
            case DISBAND:
                leavedPlayer.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_IS_DISPERSED);
                break;
        }

        if (leavedPlayer.isInInstance()) {
            ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    if (!leavedPlayer.isInGroup2() && leavedPlayer.getPosition().getWorldMapInstance().getRegisteredGroup() != null) {
                        InstanceService.moveToExitPoint(leavedPlayer);
                    }
                }
            }, 10000);
        }
    }

    @Override
    public boolean apply(PlayerGroupMember member) {
        Player player = member.getObject();
        player.sendPck(new SM_GROUP_MEMBER_INFO(team, leavedPlayer, GroupEvent.LEAVE));

        switch (reason) {
            case LEAVE:
            case DISBAND:
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_HE_LEAVE_PARTY(leavedPlayer.getName()));
                break;
            case BAN:
                // TODO find out empty strings (Retail has +2 empty strings
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_HE_IS_BANISHED(leavedPlayer.getName()));
                break;
        }

        return true;
    }

}
