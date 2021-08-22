/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group.events;

import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.common.events.ChangeLeaderEvent;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public class ChangeGroupLeaderEvent extends ChangeLeaderEvent<PlayerGroup> {

    public ChangeGroupLeaderEvent(PlayerGroup team, Player eventPlayer) {
        super(team, eventPlayer);
    }

    public ChangeGroupLeaderEvent(PlayerGroup team) {
        super(team, null);
    }

    @Override
    public void handleEvent() {
        Player oldLeader = team.getLeaderObject();
        if (eventPlayer == null) {
            team.applyOnMembers(this);
        } else {
            changeLeaderTo(eventPlayer);
        }
        checkLeaderChanged(oldLeader);
    }

    @Override
    protected void changeLeaderTo(final Player player) {
        team.changeLeader(team.getMember(player.getObjectId()));
        team.applyOnMembers(new Predicate<Player>() {

            @Override
            public boolean apply(Player member) {
                member.sendPck(new SM_GROUP_INFO(team));
                if (!player.equals(member)) {
                    member.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_HE_IS_NEW_LEADER(player.getName()));
                } else {
                    member.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_YOU_BECOME_NEW_LEADER);
                }
                return true;
            }

        });
    }

}
