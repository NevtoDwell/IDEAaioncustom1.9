/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.handlers;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.Handler;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public abstract class CmdReloadHandler implements Handler<Tuple2<Player, String[]>> {

    @Override
    public int getPriority() {
        return 0;
    }

    @NotNull
    @Override
    public final String getType() {
        return CmdReloadHandler.class.getName();
    }

}
