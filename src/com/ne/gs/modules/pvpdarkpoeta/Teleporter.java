/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.pvpdarkpoeta;

import com.ne.commons.func.tuple.Tuple3;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.modules.common.CustomLocManager;
import com.ne.gs.modules.common.Pos;
import com.ne.gs.modules.common.TeleportHandler;

import static com.ne.gs.modules.common.CustomLocManager.Teleport;

/**
 * @author hex1r0
 */
public class Teleporter implements TeleportHandler<Tuple3<Player, Pos, TeleportHandler.TeleportCallback>> {
    @Override
    public void teleport(Tuple3<Player, Pos, TeleportCallback>  e) {
        CustomLocManager.getInstance().tell(new Teleport(e));
    }
}
