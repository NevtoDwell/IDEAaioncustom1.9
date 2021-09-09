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
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.audit.AuditLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ATracer
 */
public class FlyController {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(FlyController.class);

    private static final long FLY_REUSE_TIME = 10000;
    private final Player player;
    private final ActionObserver glideObserver = new ActionObserver(ObserverType.ABNORMALSETTED) {

        @Override
        public void abnormalsetted(AbnormalState state) {
            if ((state.getId() & AbnormalState.CANT_MOVE_STATE.getId()) > 0 && !player.isInvulnerableWing()) {
                player.getFlyController().onStopGliding(true);
            }
        }
    };

    public FlyController(Player player) {
        this.player = player;
    }

    /**
     *
     */   
    public void onStopGliding(boolean removeWings) {
        if (player.isInState(CreatureState.GLIDING)) {
            player.unsetState(CreatureState.GLIDING);
            PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.STOP_GLIDE, 0, 0), true);
            
            if (player.isInState(CreatureState.FLYING)) {
                player.setFlyState(1);
            } else {
                player.setFlyState(0);
                if(!player.isInSprintMode()) {
                	player.getLifeStats().triggerFpRestore();
                }
                if (removeWings) {
                    PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.LAND, 0, 0), true);
                }
            }

            player.getObserveController().removeObserver(glideObserver);
            player.getGameStats().updateStatsAndSpeedVisually();
        }
    }

    /**
     * Ends flying 1) by CM_EMOTION (pageDown or fly button press) 2) from server side during teleportation (abyss gates should not break flying) 3) when FP is
     * decreased to 0
     */
    public void endFly(boolean forceEndFly) {
        if (player.isInState(CreatureState.FLYING) || player.isInState(CreatureState.GLIDING)) {
            player.unsetState(CreatureState.FLYING);
            player.unsetState(CreatureState.GLIDING);
            player.unsetState(CreatureState.FLOATING_CORPSE);
            player.setFlyState(0);
            player.setAfterFlying(0);

            // this is probably needed to change back fly speed into speed.
            // TODO remove this and just send in update?
            PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_EMOTE2, 0, 0), true);
            if (forceEndFly) {
                PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.LAND, 0, 0), true);
            }

            player.getObserveController().removeObserver(glideObserver);
            player.getGameStats().updateStatsAndSpeedVisually();

            player.getLifeStats().triggerFpRestore();
        }
    }

    /**
     * This method is called to start flying (called by CM_EMOTION when pageUp or pressed fly button)
     */
    public void startFly() {
        if (player.getFlyReuseTime() > System.currentTimeMillis()) {
            AuditLogger.info(player, "No Flight Cooldown Hack. Reuse time: " + ((player.getFlyReuseTime() - System.currentTimeMillis()) / 1000));
            return;
        }
        player.setFlyReuseTime(System.currentTimeMillis() + FLY_REUSE_TIME);
        player.setState(CreatureState.FLYING);
        if (player.isInPlayerMode(PlayerMode.RIDE)) {
            player.setState(CreatureState.FLOATING_CORPSE);
        }
        player.setFlyState(1);
        player.setAfterFlying(1);
        player.getLifeStats().triggerFpReduce();
        // TODO remove it?
        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_EMOTE2, 0, 0), true);
        player.getGameStats().updateStatsAndSpeedVisually();
    }

    /**
     * Switching to glide mode (called by CM_MOVE with VALIDATE_GLIDE movement type)
     * 1) from standing state
     * 2) from flying state If from stand to glide - start
     * fp reduce + emotions/stats if from fly to glide - only emotions/stats
     */
    //0: regular, 1: fly, 2: glide
    
    public boolean switchToGliding() {
        if (!player.isInState(CreatureState.GLIDING)) {                         
            if (player.getFlyState() == 0) {
                if (player.getFlyReuseTime() > System.currentTimeMillis()) {
                    return false;
                }
                player.setFlyReuseTime(System.currentTimeMillis() + FLY_REUSE_TIME);
                player.getLifeStats().triggerFpReduce();
            }
            player.setFlyState(2);
            player.setState(CreatureState.GLIDING);

            player.getObserveController().addObserver(glideObserver);
            player.getGameStats().updateStatsAndSpeedVisually();
        }
        return true;
    }
}
