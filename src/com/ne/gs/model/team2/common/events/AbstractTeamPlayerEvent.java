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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.TeamEvent;
import com.ne.gs.model.team2.TemporaryPlayerTeam;

/**
 * @author ATracer
 */
public abstract class AbstractTeamPlayerEvent<T extends TemporaryPlayerTeam<?>> implements Predicate<Player>, TeamEvent {

    protected final T team;
    protected final Player eventPlayer;

    public AbstractTeamPlayerEvent(T team, Player eventPlayer) {
        this.team = team;
        this.eventPlayer = eventPlayer;
    }
}
