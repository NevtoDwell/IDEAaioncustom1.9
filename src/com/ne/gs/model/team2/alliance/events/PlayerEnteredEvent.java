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
import com.ne.gs.model.team2.TeamEvent;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceMember;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.common.legacy.PlayerAllianceEvent;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SHOW_BRAND;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public class PlayerEnteredEvent implements Predicate<PlayerAllianceMember>, TeamEvent {

    private final PlayerAlliance alliance;
    private final Player invited;
    private PlayerAllianceMember invitedMember;

    public PlayerEnteredEvent(PlayerAlliance alliance, Player player) {
        this.alliance = alliance;
        invited = player;
    }

    /**
     * Entered player should not be in group yet
     */
    @Override
    public boolean checkCondition() {
        return !alliance.hasMember(invited.getObjectId());
    }

    @Override
    public void handleEvent() {
        PlayerAllianceService.addPlayerToAlliance(alliance, invited);

        invitedMember = alliance.getMember(invited.getObjectId());

        invited.sendPck(new SM_ALLIANCE_INFO(alliance));
        invited.sendPck(new SM_SHOW_BRAND(0, 0));
        invited.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_ENTERED_FORCE);
        invited.sendPck(new SM_ALLIANCE_MEMBER_INFO(invitedMember, PlayerAllianceEvent.JOIN));

        alliance.apply(this);
    }

    @Override
    public boolean apply(PlayerAllianceMember member) {
        Player player = member.getObject();
        if (!invited.getObjectId().equals(player.getObjectId())) {
            player.sendPck(new SM_ALLIANCE_MEMBER_INFO(invitedMember, PlayerAllianceEvent.JOIN));
            player.sendPck(new SM_INSTANCE_INFO(invited, false, alliance));
            player.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_HE_ENTERED_FORCE(invited.getName()));

            invited.sendPck(new SM_ALLIANCE_MEMBER_INFO(member, PlayerAllianceEvent.ENTER));
        }
        return true;
    }

}
