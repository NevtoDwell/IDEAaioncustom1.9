/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */

package com.ne.gs.model.handlers;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.Handler;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_SPAWN;
import com.ne.gs.services.player.PlayerReviveService;
import com.ne.gs.utils.PacketSendUtility;

/**
 * This class ...
 *
 * @author hex1r0
 */
public abstract class EffectResurrectBaseHandler implements Handler<Tuple2<Player, Integer>> {

    public static final EffectResurrectBaseHandler STATIC = new EffectResurrectBaseHandler() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Integer> e) {
            Player effected = e._1;
            int skillId = e._2;

            if (effected.isInInstance()) {
                PlayerReviveService.instanceRevive(effected, skillId);
            } else if (effected.getKisk() != null) {
                PlayerReviveService.kiskRevive(effected, skillId);
            } else {
                PlayerReviveService.bindRevive(effected, skillId);
            }
            PacketSendUtility.broadcastPacket(effected, new SM_EMOTION(effected, EmotionType.RESURRECT), true);
            PacketSendUtility.sendPck(effected, new SM_PLAYER_SPAWN(effected));

            return true;
        }
    };


    @NotNull
    @Override
    public final String getType() {
        return EffectResurrectBaseHandler.class.getName();
    }

    @Override
    public int getPriority() { return 0; }

}