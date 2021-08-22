package com.ne.gs.model.events;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.utils.TypedCallback;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public abstract class PlayerTeleported implements TypedCallback<Player, Object> {
    @NotNull
    @Override
    public final String getType() {
        return PlayerTeleported.class.getName();
    }
}
