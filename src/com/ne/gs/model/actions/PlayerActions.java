/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.actions;

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.player.InRoll;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.templates.ride.RideInfo;
import com.ne.gs.model.templates.windstreams.WindstreamPath;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.utils.PacketSendUtility;

public final class PlayerActions {

    public static boolean isInPlayerMode(Player player, PlayerMode mode) {
        switch (mode) {
            case RIDE:
                return player.ride != null;
            case IN_ROLL:
                return player.inRoll != null;
            case WINDSTREAM_STARTED:
                return player.windstreamPath != null;
        }
        return false;
    }

    public static void setPlayerMode(Player player, PlayerMode mode, Object obj) {
        switch (mode) {
            case RIDE:
                player.ride = (RideInfo) obj;
                break;
            case IN_ROLL:
                player.inRoll = (InRoll) obj;
                break;
            case WINDSTREAM_STARTED:
                player.windstreamPath = (WindstreamPath) obj;
        }
    }

    public static boolean unsetPlayerMode(Player player, PlayerMode mode) {
        switch (mode) {
            case RIDE:
                RideInfo ride = player.ride;
                if (ride == null) {
                    return false;
                }

                if (player.isInSprintMode()) {
                    player.getLifeStats().triggerFpRestore();
                    player.setSprintMode(false);
                }
                player.unsetState(CreatureState.RESTING);
                player.unsetState(CreatureState.FLOATING_CORPSE);
                player.setState(CreatureState.ACTIVE);
                player.ride = null;
                PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_EMOTE2, 0, 0), true);
                PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RIDE_END), true);

                player.getGameStats().updateStatsAndSpeedVisually();

                for (ActionObserver observer : player.getRideObservers()) {
                    player.getObserveController().removeObserver(observer);
                }
                player.getRideObservers().clear();
                return true;
            case IN_ROLL:
                if (player.inRoll == null) {
                    return false;
                }
                player.inRoll = null;
                return true;
            case WINDSTREAM_STARTED:
                if (player.windstreamPath == null) {
                    return false;
                }
                player.windstreamPath = null;
                return true;
        }
        return false;
    }
}
