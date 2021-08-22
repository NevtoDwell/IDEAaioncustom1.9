package com.ne.gs.model.events;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple3;
import com.ne.commons.utils.EventNotifier;
import com.ne.commons.utils.TypedCallback;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.world.WorldMapInstance;

/**
 * This class ...
 *
 * @author hex1r0
 */
public abstract class PlayerUsedSkill implements TypedCallback<Tuple3<Player, VisibleObject, Integer>, Object> {
    static {
        EventNotifier.GLOBAL.attach(new PlayerUsedSkill() {
            @Override
            public Object onEvent(@NotNull Tuple3<Player, VisibleObject, Integer> e) {
                Player player = e._1;
                VisibleObject target = e._2;
                int skillId = e._3;
                WorldMapInstance wmi = player.getPosition().getWorldMapInstance();

                // [1] redirection to object notifier
                player.getNotifier().fire(PlayerUsedSkill.class, e);

                // [2] world map handler notification
                //wmi.getInstanceHandler().onPlayerUsedSkill(player, target, skillId);

                // [3] world map AIs notification
                //wmi.doOnAllNpcs(npc -> npc.getAi2().onPlayerUsedSkill(player, target, skillId));

                return null;
            }
        });
    }

    @NotNull
    @Override
    public final String getType() {
        return PlayerUsedSkill.class.getName();
    }
}
