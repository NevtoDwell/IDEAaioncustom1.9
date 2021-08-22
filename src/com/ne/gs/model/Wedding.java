/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author synchro2
 */

public class Wedding {

    private final Player player;
    private final Player partner;
    private final Player priest;
    private boolean accepted;

    public Wedding(Player player, Player partner, Player priest) {
        super();
        this.player = player;
        this.partner = partner;
        this.priest = priest;
    }

    public void setAccept() {
        accepted = true;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getPartner() {
        return partner;
    }

    public Player getPriest() {
        return priest;
    }

    public boolean isAccepted() {
        return accepted;
    }

}
