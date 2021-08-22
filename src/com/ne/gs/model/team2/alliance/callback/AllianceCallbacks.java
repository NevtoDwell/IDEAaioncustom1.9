/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance.callback;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.TypedCallback;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;

/**
 * @author ATracer
 */
public interface AllianceCallbacks {
    public static abstract class BeforeEnter implements TypedCallback<Tuple2<PlayerAlliance, Player>, Object> {
        @NotNull
        @Override
        public final String getType() {
            return BeforeEnter.class.getName();
        }
    }

    public static abstract class AfterEnter implements TypedCallback<Tuple2<PlayerAlliance, Player>, Object> {
        @NotNull
        @Override
        public final String getType() {
            return AfterEnter.class.getName();
        }
    }

    public static abstract class BeforeCreate implements TypedCallback<Player, Object> {
        @NotNull
        @Override
        public final String getType() {
            return BeforeCreate.class.getName();
        }
    }

    public static abstract class AfterCreate implements TypedCallback<Player, Object> {
        @NotNull
        @Override
        public final String getType() {
            return AfterCreate.class.getName();
        }
    }

    public static abstract class BeforeDisband implements TypedCallback<PlayerAlliance, Object> {
        @NotNull
        @Override
        public final String getType() {
            return BeforeDisband.class.getName();
        }
    }

    public static abstract class AfterDisband implements TypedCallback<PlayerAlliance, Object> {
        @NotNull
        @Override
        public final String getType() {
            return AfterDisband.class.getName();
        }
    }
}

