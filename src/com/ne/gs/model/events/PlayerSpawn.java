/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2014, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.events;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.utils.TypedCallback;
import com.ne.gs.instance.handlers.InstanceHandler;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * This class ...
 *
 * @author hex1r0
 */
public abstract class PlayerSpawn implements TypedCallback<Player, Object> {
    @NotNull
    @Override
    public final String getType() {
        return PlayerSpawn.class.getName();
    }

    public static abstract class PlayerSpawnChannelBound extends PlayerSpawn {
        private InstanceHandler _instanceHandler;

        public PlayerSpawnChannelBound(InstanceHandler instanceHandler) {
            _instanceHandler = instanceHandler;
        }

        @Override
        public final Object onEvent(@NotNull Player e) {
            InstanceHandler ih = e.getPosition().getWorldMapInstance().getInstanceHandler();
            //if (ih instanceof InstanceHandlerExceptionWrapper)
                //ih = ((InstanceHandlerExceptionWrapper) ih).getInstanceHandler();

            if (ih != _instanceHandler)
                return null;

            onEventImpl(e);
            return null;
        }

        public abstract void onEventImpl(@NotNull Player e);
    }
}
