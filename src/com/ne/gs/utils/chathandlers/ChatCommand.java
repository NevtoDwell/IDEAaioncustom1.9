/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.chathandlers;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.utils.AbstractCommand;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public abstract class ChatCommand extends AbstractCommand<Player> {

    @Override
    protected abstract void runImpl(@NotNull Player player, @NotNull String alias, @NotNull String... params) throws Exception;

    @Override
    public void onError(Player player, Exception e) {
        super.onError(player, e);
    }
}
