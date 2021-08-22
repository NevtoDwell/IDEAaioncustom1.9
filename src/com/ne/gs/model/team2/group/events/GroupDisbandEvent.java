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
import com.ne.gs.model.team2.common.events.PlayerLeavedEvent.LeaveReson;
import com.ne.gs.model.team2.group.PlayerGroup;

/**
 * @author ATracer
 */
public class GroupDisbandEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

    private final PlayerGroup group;

    /**
     * @param group
     */
    public GroupDisbandEvent(PlayerGroup group) {
        this.group = group;
    }

    @Override
    public void handleEvent() {
        group.applyOnMembers(this);
    }

    @Override
    public boolean apply(Player player) {
        group.onEvent(new PlayerGroupLeavedEvent(group, player, LeaveReson.DISBAND));
        return true;
    }

}
