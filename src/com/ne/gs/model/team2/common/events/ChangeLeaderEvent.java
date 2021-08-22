/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.common.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.TemporaryPlayerTeam;
import com.ne.gs.model.team2.group.events.ChangeGroupLeaderEvent;

/**
 * @author ATracer
 */
public abstract class ChangeLeaderEvent<T extends TemporaryPlayerTeam<?>> extends AbstractTeamPlayerEvent<T> {

    private static final Logger log = LoggerFactory.getLogger(ChangeGroupLeaderEvent.class);

    public ChangeLeaderEvent(T team, Player eventPlayer) {
        super(team, eventPlayer);
    }

    /**
     * New leader either is null or should be online
     */
    @Override
    public boolean checkCondition() {
        return eventPlayer == null || eventPlayer.isOnline();
    }

    @Override
    public boolean apply(Player player) {
        if (!player.getObjectId().equals(team.getLeader().getObjectId()) && player.isOnline()) {
            changeLeaderTo(player);
            return false;
        }
        return true;
    }

    /**
     * @param oldLeader
     */
    protected void checkLeaderChanged(Player oldLeader) {
        if (team.isLeader(oldLeader)) {
            log.info("TEAM2: leader is not changed, total: {}, online: {}", team.size(), team.onlineMembers());
        }
    }

    protected abstract void changeLeaderTo(Player player);

}
