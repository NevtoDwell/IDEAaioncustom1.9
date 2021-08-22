/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance.events;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.TeamEvent;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceMember;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.common.legacy.PlayerAllianceEvent;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public class PlayerDisconnectedEvent implements TeamEvent, Predicate<PlayerAllianceMember> {

    private final PlayerAlliance alliance;
    private final Player disconnected;
    private final PlayerAllianceMember disconnectedMember;

    public PlayerDisconnectedEvent(PlayerAlliance alliance, Player player) {
        this.alliance = alliance;
        disconnected = player;
        disconnectedMember = alliance.getMember(disconnected.getObjectId());
    }

    /**
     * Player should be in alliance before disconnection
     */
    @Override
    public boolean checkCondition() {
        return alliance.hasMember(disconnected.getObjectId());
    }

    @Override
    public void handleEvent() {
        Preconditions.checkNotNull(disconnectedMember, "Disconnected member should not be null");
        alliance.apply(this);
        if (alliance.onlineMembers() == 0) {
            PlayerAllianceService.disband(alliance);
        } else if (disconnected.equals(alliance.getLeader().getObject())) {
            alliance.onEvent(new ChangeAllianceLeaderEvent(alliance));
        }
    }

    @Override
    public boolean apply(PlayerAllianceMember member) {
        Player player = member.getObject();
        if (!disconnected.getObjectId().equals(player.getObjectId())) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_HE_BECOME_OFFLINE(disconnected.getName()));
            player.sendPck(new SM_ALLIANCE_MEMBER_INFO(disconnectedMember, PlayerAllianceEvent.DISCONNECTED));
        }
        return true;
    }

}
