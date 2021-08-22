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

import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceGroup;
import com.ne.gs.model.team2.alliance.PlayerAllianceMember;
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.common.legacy.PlayerAllianceEvent;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_MEMBER_INFO;

/**
 * @author ATracer
 */
public class ChangeMemberGroupEvent extends AlwaysTrueTeamEvent implements Predicate<PlayerAllianceMember> {

    private final PlayerAlliance alliance;
    private final int firstMemberId;
    private final int secondMemberId;
    private final int allianceGroupId;

    private PlayerAllianceMember firstMember;
    private PlayerAllianceMember secondMember;

    public ChangeMemberGroupEvent(PlayerAlliance alliance, int firstMemberId, int secondMemberId, int allianceGroupId) {
        this.alliance = alliance;
        this.firstMemberId = firstMemberId;
        this.secondMemberId = secondMemberId;
        this.allianceGroupId = allianceGroupId;
    }

    @Override
    public void handleEvent() {
        firstMember = alliance.getMember(firstMemberId);
        secondMember = alliance.getMember(secondMemberId);
        Preconditions.checkNotNull(firstMember, "First member should not be null");
        Preconditions.checkArgument(secondMemberId == 0 || secondMember != null, "Second member should not be null");
        if (secondMember != null) {
            swapMembersInGroup(firstMember, secondMember);
        } else {
            moveMemberToGroup(firstMember, allianceGroupId);
        }
        alliance.apply(this);
    }

    @Override
    public boolean apply(PlayerAllianceMember member) {
        member.getObject().sendPck(new SM_ALLIANCE_MEMBER_INFO(firstMember, PlayerAllianceEvent.MEMBER_GROUP_CHANGE));
        if (secondMember != null) {
            member.getObject().sendPck(new SM_ALLIANCE_MEMBER_INFO(secondMember, PlayerAllianceEvent.MEMBER_GROUP_CHANGE));
        }
        return true;
    }

    private void swapMembersInGroup(PlayerAllianceMember firstMember, PlayerAllianceMember secondMember) {
        PlayerAllianceGroup firstAllianceGroup = firstMember.getPlayerAllianceGroup();
        PlayerAllianceGroup secondAllianceGroup = secondMember.getPlayerAllianceGroup();
        firstAllianceGroup.removeMember(firstMember);
        secondAllianceGroup.removeMember(secondMember);
        firstAllianceGroup.addMember(secondMember);
        secondAllianceGroup.addMember(firstMember);
    }

    private void moveMemberToGroup(PlayerAllianceMember firstMember, int allianceGroupId) {
        PlayerAllianceGroup firstAllianceGroup = firstMember.getPlayerAllianceGroup();
        firstAllianceGroup.removeMember(firstMember);
        PlayerAllianceGroup newAllianceGroup = alliance.getAllianceGroup(allianceGroupId);
        newAllianceGroup.addMember(firstMember);
    }
}
