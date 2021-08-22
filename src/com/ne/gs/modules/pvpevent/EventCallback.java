package com.ne.gs.modules.pvpevent;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.Handler;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public abstract class EventCallback implements Handler<Tuple2<Player, String>> {

    public abstract static class Apply extends EventCallback {
        @NotNull
        @Override
        public String getType() {
            return Apply.class.getName();
        }
    }

    public abstract static class Cancel extends EventCallback {
        @NotNull
        @Override
        public String getType() {
            return Cancel.class.getName();
        }
    }

    @Override
    public int getPriority() { return 0; }
}
