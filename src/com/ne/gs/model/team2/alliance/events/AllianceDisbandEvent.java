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
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.common.events.PlayerLeavedEvent.LeaveReson;

/**
 * @author ATracer
 */
public class AllianceDisbandEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

    private final PlayerAlliance alliance;

    /**
     * @param alliance
     */
    public AllianceDisbandEvent(PlayerAlliance alliance) {
        this.alliance = alliance;
    }

    @Override
    public void handleEvent() {
        alliance.applyOnMembers(this);
    }

    @Override
    public boolean apply(Player player) {
        alliance.onEvent(new PlayerAllianceLeavedEvent(alliance, player, LeaveReson.DISBAND));
        return true;
    }

}
