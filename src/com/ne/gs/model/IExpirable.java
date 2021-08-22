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
 * @author Mr. Poke
 */
public interface IExpirable {

    public int getExpireTime();

    public void expireEnd(Player player);

    public boolean canExpireNow();

    public void expireMessage(Player player, int time);
}
