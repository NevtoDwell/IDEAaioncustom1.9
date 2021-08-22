/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance.events;

import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceMember;
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.common.legacy.PlayerAllianceEvent;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SHOW_BRAND;

/**
 * @author ATracer
 */
public class PlayerConnectedEvent extends AlwaysTrueTeamEvent implements Predicate<PlayerAllianceMember> {

    private final PlayerAlliance alliance;
    private final Player connected;
    private PlayerAllianceMember connectedMember;

    public PlayerConnectedEvent(PlayerAlliance alliance, Player player) {
        this.alliance = alliance;
        connected = player;
    }

    @Override
    public void handleEvent() {
        alliance.removeMember(connected.getObjectId());
        connectedMember = new PlayerAllianceMember(connected);
        alliance.addMember(connectedMember);

        connected.sendPck(new SM_ALLIANCE_INFO(alliance));
        connected.sendPck(new SM_ALLIANCE_MEMBER_INFO(connectedMember, PlayerAllianceEvent.RECONNECT));
        connected.sendPck(new SM_SHOW_BRAND(0, 0));

        alliance.apply(this);
    }

    @Override
    public boolean apply(PlayerAllianceMember member) {
        Player player = member.getObject();
        if (!connected.getObjectId().equals(player.getObjectId())) {
            player.sendPck(new SM_ALLIANCE_MEMBER_INFO(connectedMember, PlayerAllianceEvent.RECONNECT));
            player.sendPck(new SM_INSTANCE_INFO(connected, false, alliance));

            connected.sendPck(new SM_ALLIANCE_MEMBER_INFO(member, PlayerAllianceEvent.RECONNECT));
        }
        return true;
    }

}
