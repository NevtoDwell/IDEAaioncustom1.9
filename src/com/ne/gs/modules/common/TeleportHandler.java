/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.common;

import com.ne.commons.func.tuple.Tuple3;
import com.ne.commons.utils.Callback;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.teleport.TeleportService;

/**
 * @author hex1r0
 */
public interface TeleportHandler<T> {

    public static final TeleportHandler<Tuple3<Player, Pos, TeleportCallback>> SIMPLE
        = new TeleportHandler<Tuple3<Player, Pos, TeleportCallback>>() {
        @Override
        public void teleport(Tuple3<Player, Pos, TeleportCallback> e) {
            TeleportService.teleportBeam(e._1, e._2);
            e._3.onEvent(e);
        }
    };

    void teleport(T e);

    public interface TeleportCallback extends Callback<Tuple3<Player, Pos, TeleportCallback>, Object> {}
}
