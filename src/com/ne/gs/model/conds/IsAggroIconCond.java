/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
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
public abstract class IsAggroIconCond extends SimpleCond<Tuple2<Player, Player>> {

    public static final IsAggroIconCond TRUE = new IsAggroIconCond() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
            return true;
        }
    };

    public static final IsAggroIconCond STATIC = new IsAggroIconCond() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
            Player owner = e._1;
            Player opponent = e._2;

            return !opponent.getRace().equals(owner.getRace());
        }
    };

    @NotNull
    @Override
    public final String getType() {
        return IsAggroIconCond.class.getName();
    }
}