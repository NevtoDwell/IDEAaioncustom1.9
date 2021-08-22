/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author antness
 */
public class PlayerAP implements Comparable<PlayerAP> {

    private final Player player;
    private final Race race;
    private int ap;

    public PlayerAP(Player player) {
        this.player = player;
        race = player.getRace();
        ap = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public Race getRace() {
        return race;
    }

    public int getAP() {
        return ap;
    }

    public void increaseAP(int ap) {
        this.ap += ap;
    }

    @Override
    public int compareTo(PlayerAP pl) {
        return ap - pl.ap;
    }
}
