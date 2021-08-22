/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author SoulKeeper
 * @author_fix nerolory
 */
public class CM_EMOTION extends AionClientPacket {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CM_EMOTION.class);

    private EmotionType emotionType;

    private int emotion;
    /**
     * Coordinates of player
     */
    private float x;
    private float y;
    private float z;
    private byte heading;

    private int targetObjectId;

    @Override
    protected void readImpl() {
        int et = readC();     
        emotionType = EmotionType.of(et);

        switch (emotionType) {
            case SELECT_TARGET:// select target
            case JUMP: // jump
            case SIT: // resting
            case STAND: // end resting
            case LAND_FLYTELEPORT: // fly teleport land
            case FLY: // fly up
            case LAND: // land
            case RIDE:
            case RIDE_END:
            case DIE: // die
            case ATTACKMODE: // get equip weapon
            case NEUTRALMODE: // remove equip weapon
            case END_DUEL: // duel end
            case WALK: // walk on
            case RUN: // walk off
            case OPEN_DOOR: // open static doors
                // case CLOSE_DOOR: // close static doors
            case POWERSHARD_ON: // powershard on
            case POWERSHARD_OFF: // powershard off
            case ATTACKMODE2: // get equip weapon
            case NEUTRALMODE2: // remove equip weapon
            case END_SPRINT:
            case START_SPRINT:
                break;
            case EMOTE:
                emotion = readH();
                targetObjectId = readD();
                break;
            case CHAIR_SIT: // sit on chair
            case CHAIR_UP: // stand on chair
                x = readF();
                y = readF();
                z = readF();
                heading = (byte) readC();
                break;
            default:
                log.error("Unknown emotion type? 0x" + Integer.toHexString(et/* !!!!! */).toUpperCase());
                break;
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if(player.getController().hasTask(TaskId.ITEM_USE))
            return;

        if (player.getLifeStats().isAlreadyDead()) {
            return;
        }
        if (player.getState() == CreatureState.PRIVATE_SHOP.getId() || player.isAttackMode()
            && (emotionType == EmotionType.CHAIR_SIT || emotionType == EmotionType.JUMP)) {
            return;
        }

        player.getController().cancelUseItem();
        if (emotionType != EmotionType.SELECT_TARGET) {
            player.getController().cancelCurrentSkill();
        }
        if (player.getController().isUnderStance()
            && (emotionType == EmotionType.SIT || emotionType == EmotionType.JUMP
            || emotionType == EmotionType.NEUTRALMODE
            || emotionType == EmotionType.NEUTRALMODE2 || emotionType == EmotionType.ATTACKMODE || emotionType ==
            EmotionType.ATTACKMODE2)) {
            player.getController().stopStance();
        }
        
        if (emotionType == EmotionType.JUMP) {
            player.getMoveController().updateLastJump();
        }
        switch (emotionType) {
            case SELECT_TARGET:
                return;
            case SIT:
                if (player.isInState(CreatureState.PRIVATE_SHOP) || player.isInPlayerMode(PlayerMode.RIDE)) {
                    return;
                }
                player.setState(CreatureState.RESTING);
                break;
            case STAND:
                player.unsetState(CreatureState.RESTING);
                break;
            case CHAIR_SIT:
                if (!player.isInState(CreatureState.WEAPON_EQUIPPED)) {
                    player.setState(CreatureState.CHAIR);
                }
                break;
            case CHAIR_UP:
                player.unsetState(CreatureState.CHAIR);
                break;
            case LAND_FLYTELEPORT:
                player.getController().onFlyTeleportEnd();
                break;
            case FLY:         
                if (!player.isInsideZoneType(ZoneType.FLY)) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_FLYING_FORBIDDEN_HERE);
                    return;
                }
                // If player is under NoFly Effect, show the retail message for it and return
                if (player.isUnderNoFly()) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_CANT_FLY_NOW_DUE_TO_NOFLY);
                    return;
                }
                player.getFlyController().startFly();
                break;
            case LAND:
                player.getFlyController().endFly(false);
                break;
            case ATTACKMODE2:
            case ATTACKMODE:
                player.setAttackMode(true);
                player.setState(CreatureState.WEAPON_EQUIPPED);
                break;
            case NEUTRALMODE2:
            case NEUTRALMODE:
                player.setAttackMode(false);
                player.unsetState(CreatureState.WEAPON_EQUIPPED);
                break;
            case WALK:
                // cannot toggle walk when you flying or gliding
                if (player.getFlyState() > 0) {
                    return;
                }
                player.setState(CreatureState.WALKING);
                break;
            case RUN:
                player.unsetState(CreatureState.WALKING);
                break;
            case OPEN_DOOR:
            case CLOSE_DOOR:
                break;
            case POWERSHARD_ON:
                if (!player.getEquipment().isPowerShardEquipped()) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_WEAPON_BOOST_NO_BOOSTER_EQUIPED);
                    return;
                }
                player.sendPck(SM_SYSTEM_MESSAGE.STR_WEAPON_BOOST_BOOST_MODE_STARTED);
                player.setState(CreatureState.POWERSHARD);
                break;
            case POWERSHARD_OFF:
                player.sendPck(SM_SYSTEM_MESSAGE.STR_WEAPON_BOOST_BOOST_MODE_ENDED);
                player.unsetState(CreatureState.POWERSHARD);
                break;
            case START_SPRINT:
                if (!player.isInPlayerMode(PlayerMode.RIDE) || player.getLifeStats().getCurrentFp() < player.ride
                    .getStartFp()
                    || player.isInState(CreatureState.FLYING) || !player.ride.canSprint()) {
                    return;
                }
                player.setSprintMode(true);
                player.getLifeStats().cancelFpReduce();
                player.getLifeStats().triggerFpReduceByCost(player.ride.getCostFp());
                break;
            case END_SPRINT:
                if (!player.isInPlayerMode(PlayerMode.RIDE) || !player.ride.canSprint()) {
                    return;
                }
                player.setSprintMode(false);
                player.getLifeStats().triggerFpRestore();
                break;
            case JUMP:
            case WINDSTREAM:
            case WINDSTREAM_END:
            case WINDSTREAM_EXIT:
            case WINDSTREAM_END_BOOST:
                break;
        }
        if (player.getEmotions().canUse(emotion)) {
            PacketSendUtility
                .broadcastPacket(player, new SM_EMOTION(player, emotionType, emotion, x, y, z, heading,
                    getTargetObjectId(player)), true);
        }
    }

    private int getTargetObjectId(Player player) {
        int target = player.getTarget() == null ? 0 : player.getTarget().getObjectId();
        return target != 0 ? target : targetObjectId;
    }
}
