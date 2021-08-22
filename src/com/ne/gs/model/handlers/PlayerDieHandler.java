package com.ne.gs.model.handlers;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple3;
import com.ne.commons.utils.Handler;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public abstract class PlayerDieHandler implements Handler<Tuple3<Player, Creature, Boolean>> {

    @Override
    public int getPriority() {
        return 0;
    }

    @NotNull
    @Override
    public final String getType() {
        return PlayerDieHandler.class.getName();
    }

}
