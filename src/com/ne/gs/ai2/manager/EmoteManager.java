/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.manager;

import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public final class EmoteManager {

    /**
     * Npc starts attacking from idle state
     *
     * @param owner
     */
    public static void emoteStartAttacking(Npc owner) {
        Creature target = (Creature) owner.getTarget();
        owner.unsetState(CreatureState.WALKING);
        if (!owner.isInState(CreatureState.WEAPON_EQUIPPED)) {
            owner.setState(CreatureState.WEAPON_EQUIPPED);
            PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, target.getObjectId()));
            PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.ATTACKMODE, 0, target.getObjectId()));
        }
    }

    /**
     * Npc stops attacking
     *
     * @param owner
     */
    public static void emoteStopAttacking(Npc owner) {
        owner.unsetState(CreatureState.WEAPON_EQUIPPED);
        if (owner.getTarget() != null && owner.getTarget() instanceof Player) {
            ((Player) owner.getTarget()).sendPck(SM_SYSTEM_MESSAGE.STR_UI_COMBAT_NPC_RETURN(owner.getObjectTemplate().getNameId()));
        }
    }

    /**
     * Npc starts following other creature
     *
     * @param owner
     */
    public static void emoteStartFollowing(Npc owner) {
        owner.unsetState(CreatureState.WALKING);
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.NEUTRALMODE, 0, 0));
    }

    /**
     * Npc starts walking (either random or path)
     *
     * @param owner
     */
    public static void emoteStartWalking(Npc owner) {
        owner.setState(CreatureState.WALKING);
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.WALK));
    }

    /**
     * Npc stops walking
     *
     * @param owner
     */
    public static void emoteStopWalking(Npc owner) {
        owner.unsetState(CreatureState.WALKING);
    }

    /**
     * Npc starts returning to spawn location
     *
     * @param owner
     */
    public static void emoteStartReturning(Npc owner) {
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.NEUTRALMODE, 0, 0));
    }

    /**
     * Npc starts idling
     *
     * @param owner
     */
    public static void emoteStartIdling(Npc owner) {
        owner.setState(CreatureState.WALKING);
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.NEUTRALMODE, 0, 0));
    }
}
