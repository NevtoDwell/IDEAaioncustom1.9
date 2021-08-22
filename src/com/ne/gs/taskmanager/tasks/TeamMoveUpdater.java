/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager.tasks;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.common.legacy.PlayerAllianceEvent;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.taskmanager.AbstractIterativePeriodicTaskManager;

/**
 * @author Sarynth Supports PlayerGroup and PlayerAlliance movement updating.
 */
public final class TeamMoveUpdater extends AbstractIterativePeriodicTaskManager<Player> {

    private static final class SingletonHolder {

        private static final TeamMoveUpdater INSTANCE = new TeamMoveUpdater();
    }

    public static TeamMoveUpdater getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public TeamMoveUpdater() {
        super(2000);
    }

    @Override
    protected void callTask(Player player) {
        if (player.isInGroup2()) {
            PlayerGroupService.updateGroup(player, GroupEvent.MOVEMENT);
        }
        if (player.isInAlliance2()) {
            PlayerAllianceService.updateAlliance(player, PlayerAllianceEvent.MOVEMENT);
        }

        // Remove task from list. It will be re-added if player moves again.
        stopTask(player);
    }

    @Override
    protected String getCalledMethodName() {
        return "teamMoveUpdate()";
    }

}
