package com.ne.gs.model.events;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.utils.Handler;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public abstract class ExceptBuffHandler implements Handler<Player> {
    public static final ExceptBuffHandler STATIC = new ExceptBuffHandler() {
        @Override
        public Boolean onEvent(@NotNull Player player) {
            if (player.getPosition().getWorldMapInstance().getParent().isExceptBuff()) {
                player.getEffectController().removeAllEffects();
            }

            return true;
        }
    };

    @NotNull
    @Override
    public final String getType() {
        return ExceptBuffHandler.class.getName();
    }

    @Override
    public final int getPriority() {
        return 0;
    }
}
