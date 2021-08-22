/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance.events;

import java.util.Collection;
import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceMember;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.alliance.events.AssignViceCaptainEvent.AssignType;
import com.ne.gs.model.team2.common.events.ChangeLeaderEvent;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public class ChangeAllianceLeaderEvent extends ChangeLeaderEvent<PlayerAlliance> {

    public ChangeAllianceLeaderEvent(PlayerAlliance team, Player eventPlayer) {
        super(team, eventPlayer);
    }

    public ChangeAllianceLeaderEvent(PlayerAlliance team) {
        super(team, null);
    }

    @Override
    public void handleEvent() {
        Player oldLeader = team.getLeaderObject();
        if (eventPlayer == null) {
            Collection<Integer> viceCaptainIds = team.getViceCaptainIds();
            for (Integer viceCaptainId : viceCaptainIds) {
                PlayerAllianceMember viceCaptain = team.getMember(viceCaptainId);
                if (viceCaptain.isOnline()) {
                    changeLeaderTo(viceCaptain.getObject());
                    viceCaptainIds.remove(viceCaptainId);
                    break;
                }
            }
            if (team.isLeader(oldLeader)) {
                team.applyOnMembers(this);
            }
        } else {
            changeLeaderTo(eventPlayer);
        }
        checkLeaderChanged(oldLeader);
        if (eventPlayer != null) {
            PlayerAllianceService.changeViceCaptain(oldLeader, AssignType.DEMOTE_CAPTAIN_TO_VICECAPTAIN);
        }
    }

    @Override
    protected void changeLeaderTo(final Player player) {
        team.changeLeader(team.getMember(player.getObjectId()));
        team.applyOnMembers(new Predicate<Player>() {

            @Override
            public boolean apply(Player member) {
                member.sendPck(new SM_ALLIANCE_INFO(team));
                if (!player.equals(member)) {
                    member.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_HE_IS_NEW_LEADER(player.getName()));
                } else {
                    member.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_YOU_BECOME_NEW_LEADER);
                }
                return true;
            }

        });
    }

}
