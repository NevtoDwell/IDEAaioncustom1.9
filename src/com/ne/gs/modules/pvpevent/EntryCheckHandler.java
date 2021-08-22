package com.ne.gs.modules.pvpevent;

import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public final class EntryCheckHandler {
    private EntryCheckHandler() { }

    public static boolean check(Player player) {
        return !(player.isAttackMode() || player.isInInstance() || player.isInPrison());
    }
}
