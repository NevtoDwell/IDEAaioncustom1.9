/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.common.events;

import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.TeamEvent;
import com.ne.gs.model.team2.TeamMember;
import com.ne.gs.model.team2.TemporaryPlayerTeam;

/**
 * @author ATracer
 */
public abstract class PlayerLeavedEvent<TM extends TeamMember<Player>, T extends TemporaryPlayerTeam<TM>> implements Predicate<TM>, TeamEvent {

    public static enum LeaveReson {
        BAN,
        LEAVE,
        LEAVE_TIMEOUT,
        DISBAND;
    }

    protected final T team;
    protected final Player leavedPlayer;
    protected final LeaveReson reason;
    protected final TM leavedTeamMember;
    protected final String banPersonName;

    public PlayerLeavedEvent(T alliance, Player player) {
        this(alliance, player, LeaveReson.LEAVE);
    }

    public PlayerLeavedEvent(T alliance, Player player, LeaveReson reason) {
        this(alliance, player, reason, StringUtils.EMPTY);
    }

    public PlayerLeavedEvent(T team, Player player, LeaveReson reason, String banPersonName) {
        this.team = team;
        this.leavedPlayer = player;
        this.reason = reason;
        this.leavedTeamMember = team.getMember(player.getObjectId());
        this.banPersonName = banPersonName;
    }

    /**
     * Player should be in team to broadcast this event
     */
    @Override
    public boolean checkCondition() {
        return team.hasMember(leavedPlayer.getObjectId());
    }

}
