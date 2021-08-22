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
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;

/**
 * @author ATracer
 */
public class PlayerAllianceUpdateEvent extends AlwaysTrueTeamEvent implements Predicate<PlayerAllianceMember> {

    private final PlayerAlliance alliance;
    private final Player player;
    private final PlayerAllianceEvent allianceEvent;
    private final PlayerAllianceMember updateMember;

    public PlayerAllianceUpdateEvent(PlayerAlliance alliance, Player player, PlayerAllianceEvent allianceEvent) {
        this.alliance = alliance;
        this.player = player;
        this.allianceEvent = allianceEvent;
        updateMember = alliance.getMember(player.getObjectId());
    }

    @Override
    public void handleEvent() {
        switch (allianceEvent) {
            case MOVEMENT:
            case UPDATE:
                alliance.apply(this);
                break;
            default:
                // Unsupported
                break;
        }

    }

    @Override
    public boolean apply(PlayerAllianceMember member) {
        if (!member.getObjectId().equals(player.getObjectId())) {
            member.getObject().sendPck(new SM_ALLIANCE_MEMBER_INFO(updateMember, allianceEvent));
        }
        return true;
    }

}
