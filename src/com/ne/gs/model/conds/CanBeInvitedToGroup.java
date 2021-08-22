/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2014, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.conds;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.SimpleCond;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * This class ...
 *
 * @author hex1r0
 */
public abstract class CanBeInvitedToGroup extends SimpleCond<Tuple2<Player, Player>> {

    public static final CanBeInvitedToGroup TRUE = new CanBeInvitedToGroup() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
            return true;
        }
    };

    public static final CanBeInvitedToGroup FALSE = new CanBeInvitedToGroup() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
            return false;
        }
    };

    @Override
    public String getType() {
        return CanBeInvitedToGroup.class.getName();
    }
}
