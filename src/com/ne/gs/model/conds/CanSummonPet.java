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
import com.ne.commons.utils.SimpleCond;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * This class ...
 *
 * @author hex1r0
 */
public abstract class CanSummonPet extends SimpleCond<Player> {

    public static final CanSummonPet TRUE = new CanSummonPet() {
        @Override
        public Boolean onEvent(@NotNull Player speaker) {
            return true;
        }
    };

    public static final CanSummonPet FALSE = new CanSummonPet() {
        @Override
        public Boolean onEvent(@NotNull Player e) {
            return false;
        }
    };

    @NotNull
    @Override
    public String getType() {
        return CanSummonPet.class.getName();
    }
}
