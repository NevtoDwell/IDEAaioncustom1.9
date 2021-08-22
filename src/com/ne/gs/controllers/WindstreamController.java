/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.WindstreamAction;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.templates.windstreams.WindstreamPath;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_WINDSTREAM;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.utils.PacketSendUtility;

import static com.ne.gs.utils.PacketSendUtility.broadcastPacket;

/**
 * @author IOException
 * @author ViAl
 */
public class WindstreamController {

    //private static final Logger _log = LoggerFactory.getLogger(WindstreamController.class);

    private static final int WINDSTREAM_END = 2;
    public static final int WINDSTREAM_EXIT = 3;

    private static final int START_BOOST = 7;
    private static final int END_BOOST = 8;

    private final Player player;
    private final ActionObserver windstreamObserver = new ActionObserver(ObserverType.ABNORMALSETTED) {
        @Override
        public void abnormalsetted(AbnormalState state) {
            boolean cantMoveState = (state.getId() & AbnormalState.CANT_MOVE_STATE.getId()) == AbnormalState.CANT_MOVE_STATE.getId();
            boolean fearState = (state.getId() & AbnormalState.FEAR.getId()) == AbnormalState.FEAR.getId();
            if ((cantMoveState || fearState)) {
              updateAndFixStream();
            }
        }
    };

    public void updateAndFixStream(){
        player.getWindstreamControllder().exitWindstream(WINDSTREAM_EXIT);
        broadcastPacket(player, new SM_EMOTION(player, EmotionType.LAND_FLYTELEPORT));
        broadcastPacket(player, new SM_EMOTION(player, EmotionType.LAND));
        broadcastPacket(player, new SM_EMOTION(player, EmotionType.WINDSTREAM_EXIT, 0, 0));
        //player.clearKnownlist();
        player.sendPck(new SM_PLAYER_INFO(player, false));
        //player.updateKnownlist();
        for (VisibleObject vo : player.getKnownList().getKnownObjects().values()) {
            player.getController().see(vo);
        }
        player.unsetState(CreatureState.ENTERED_WINDS);
    }
    
    public WindstreamController(Player player) {
        this.player = player;
    }

    public void enterWindstream(int teleportId, int distance) {
        player.setState(CreatureState.ENTERED_WINDS);
        player.sendPck(new SM_WINDSTREAM(WindstreamAction.ENTER.getId(), 1));
        player.getObserveController().addObserver(windstreamObserver);
        //broadcastPacket(player, new SM_EMOTION(player, EmotionType.WINDSTREAM, teleportId, distance), true);
    }

    public void startWindstream(int teleportId, int distance) {
        if (!player.isInState(CreatureState.GLIDING)) {
            return; // TODO just returning is stupid, consider teleporting back, etc
        }
        if (player.isInPlayerMode(PlayerMode.WINDSTREAM_STARTED)) {
            return; // TODO just returning is stupid, consider teleporting back, etc
        }
        if (!player.isInState(CreatureState.ENTERED_WINDS)) {
            return; // TODO just returning is stupid, consider teleporting back, etc
        }
        player.setPlayerMode(PlayerMode.WINDSTREAM_STARTED, new WindstreamPath(teleportId, distance));
        player.unsetState(CreatureState.ACTIVE);
        player.unsetState(CreatureState.GLIDING);
        player.unsetState(CreatureState.ENTERED_WINDS);
        player.setState(CreatureState.FLYING);
        broadcastPacket(player, new SM_EMOTION(player, EmotionType.WINDSTREAM, teleportId, distance), true);
        player.getLifeStats().triggerFpRestore();
    }

    public void exitWindstream(int mode) {
        //if(!player.isInPlayerMode(PlayerMode.WINDSTREAM_ENTERED) && !player.isInPlayerMode(PlayerMode.WINDSTREAM_STARTED))
        //	return;
        player.unsetState(CreatureState.FLYING);
        player.setState(CreatureState.ACTIVE);
        if (mode == WINDSTREAM_END) {
            player.setState(CreatureState.GLIDING);
            player.getLifeStats().triggerFpReduce();
        }

        broadcastPacket(player, new SM_EMOTION(player, mode == WINDSTREAM_END ?
            EmotionType.WINDSTREAM_END : EmotionType.WINDSTREAM_EXIT, 0, 0), true);

        player.getGameStats().updateStatsAndSpeedVisually();
        player.sendPck(new SM_WINDSTREAM(mode, 1));
        player.unsetPlayerMode(PlayerMode.WINDSTREAM_STARTED);
        player.getObserveController().removeObserver(windstreamObserver);
    }

    public void startBoost() {
        broadcastPacket(player, new SM_EMOTION(player, EmotionType.WINDSTREAM_START_BOOST, 0, 0), true);
        player.sendPck(new SM_WINDSTREAM(START_BOOST, 1));
    }

    public void endBoost() {
        broadcastPacket(player, new SM_EMOTION(player, EmotionType.WINDSTREAM_END_BOOST, 0, 0), true);
        player.sendPck(new SM_WINDSTREAM(END_BOOST, 1));
    }
}
