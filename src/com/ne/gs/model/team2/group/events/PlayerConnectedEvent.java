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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupMember;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_INFO;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_INSTANCE_INFO;

/**
 * @author ATracer
 */
public class PlayerConnectedEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

    private static final Logger log = LoggerFactory.getLogger(PlayerConnectedEvent.class);
    private final PlayerGroup group;
    private final Player player;

    public PlayerConnectedEvent(PlayerGroup group, Player player) {
        this.group = group;
        this.player = player;
    }

    @Override
    public void handleEvent() {
        group.removeMember(player.getObjectId());
        group.addMember(new PlayerGroupMember(player));
        // TODO this probably should never happen
        if (player.sameObjectId(group.getLeader().getObjectId())) {
            log.warn("[TEAM2] leader connected {}", group.size());
            group.changeLeader(new PlayerGroupMember(player));
        }
        player.sendPck(new SM_GROUP_INFO(group));
        player.sendPck(new SM_GROUP_MEMBER_INFO(group, player, GroupEvent.JOIN));
        group.applyOnMembers(this);
    }

    @Override
    public boolean apply(Player member) {
        if (!player.equals(member)) {
            member.sendPck(new SM_GROUP_MEMBER_INFO(group, player, GroupEvent.ENTER));
            member.sendPck(new SM_INSTANCE_INFO(player, false, group));
            player.sendPck(new SM_GROUP_MEMBER_INFO(group, member, GroupEvent.ENTER));
        }
        return true;
    }

}
