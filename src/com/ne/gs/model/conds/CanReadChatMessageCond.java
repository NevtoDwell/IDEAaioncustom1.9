package com.ne.gs.model.conds;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple4;
import com.ne.commons.utils.SimpleCond;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.model.ChatType;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * This class ...
 *
 * @author hex1r0
 */
public abstract class CanReadChatMessageCond extends SimpleCond<Tuple4<Race, ChatType, Player, Player>> {

    public static final CanReadChatMessageCond STATIC = new CanReadChatMessageCond() {
        @Override
        public Boolean onEvent(@NotNull Tuple4<Race, ChatType, Player, Player> e) {
            Race race = e._1;
            ChatType chatType = e._2;
            Player speaker = e._3;
            Player receiver = e._4;

            boolean canRead = true;
            if (race != null) {
                canRead = chatType.isSysMsg() || CustomConfig.SPEAKING_BETWEEN_FACTIONS || speaker.getAccessLevel() > 0
                    || (receiver != null && receiver.getAccessLevel() > 0);
            }
            return canRead;
        }
    };

    @NotNull
    @Override
    public String getType() {
        return CanReadChatMessageCond.class.getName();
    }

}
