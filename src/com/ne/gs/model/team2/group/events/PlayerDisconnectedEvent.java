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
import com.ne.gs.model.team2.TeamEvent;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public class PlayerDisconnectedEvent implements Predicate<Player>, TeamEvent {

    private final PlayerGroup group;
    private final Player player;

    public PlayerDisconnectedEvent(PlayerGroup group, Player player) {
        this.group = group;
        this.player = player;
    }

    /**
     * Player should be in group before disconnection
     */
    @Override
    public boolean checkCondition() {
        return group.hasMember(player.getObjectId());
    }

    @Override
    public void handleEvent() {
        if (group.onlineMembers() <= 1) {
            PlayerGroupService.disband(group);
        } else {
            if (player.equals(group.getLeader().getObject())) {
                group.onEvent(new ChangeGroupLeaderEvent(group));
            }
            group.applyOnMembers(this);
        }
    }

    @Override
    public boolean apply(Player member) {
        if (!member.equals(player)) {
            member.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_HE_BECOME_OFFLINE(player.getName()));
            member.sendPck(new SM_GROUP_MEMBER_INFO(group, player, GroupEvent.DISCONNECTED));
            // disconnect other group members on logout? check
            player.sendPck(new SM_GROUP_MEMBER_INFO(group, member, GroupEvent.DISCONNECTED));
        }
        return true;
    }

}
