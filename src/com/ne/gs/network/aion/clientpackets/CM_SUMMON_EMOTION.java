/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class CM_SUMMON_EMOTION extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_SUMMON_EMOTION.class);

    @SuppressWarnings("unused")
    private int objId;

    private int emotionTypeId;

    @Override
    protected void readImpl() {
        objId = readD();
        emotionTypeId = readC();
    }

    @Override
    protected void runImpl() {

        Player player = getConnection().getActivePlayer();
        EmotionType emotionType = EmotionType.of(emotionTypeId);

        // Unknown Summon Emotion Type
        if (emotionType == EmotionType.UNK) {
            log.error("Unknown emotion type? 0x" + Integer.toHexString(emotionTypeId).toUpperCase());
        }

        Summon summon = player.getSummon();
        if (summon == null) {
            log.warn("summon emotion without active summon on " + player.getName() + ".");
            return;
        }

        switch (emotionType) {
            case FLY:
            case LAND:
                PacketSendUtility.broadcastPacket(summon, new SM_EMOTION(summon, EmotionType.START_EMOTE2));
                PacketSendUtility.broadcastPacket(summon, new SM_EMOTION(summon, emotionType));
                break;
            case ATTACKMODE: // start attacking
                summon.setState(CreatureState.WEAPON_EQUIPPED);
                PacketSendUtility.broadcastPacket(summon, new SM_EMOTION(summon, emotionType));
                break;
            case NEUTRALMODE: // stop attacking
                summon.unsetState(CreatureState.WEAPON_EQUIPPED);
                PacketSendUtility.broadcastPacket(summon, new SM_EMOTION(summon, emotionType));
                break;
        }
    }
}
