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
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;

/**
 * @author ATracer
 */
public class PlayerGroupUpdateEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

    private final PlayerGroup group;
    private final Player player;
    private final GroupEvent groupEvent;

    public PlayerGroupUpdateEvent(PlayerGroup group, Player player, GroupEvent groupEvent) {
        this.group = group;
        this.player = player;
        this.groupEvent = groupEvent;
    }

    @Override
    public void handleEvent() {
        group.applyOnMembers(this);
    }

    @Override
    public boolean apply(Player member) {
        if (!player.equals(member)) {
            member.sendPck(new SM_GROUP_MEMBER_INFO(group, player, groupEvent));
        }
        return true;
    }

}
