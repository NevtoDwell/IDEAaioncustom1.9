/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.events;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.TypedCallback;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public abstract class PlayerEnteredMap implements TypedCallback<Tuple2<Player, Integer>, Object> {
    @NotNull
    @Override
    public final String getType() {
        return PlayerEnteredMap.class.getName();
    }
}
