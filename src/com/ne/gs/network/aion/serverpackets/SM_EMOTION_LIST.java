/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;

import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.model.gameobjects.player.emotion.Emotion;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

public class SM_EMOTION_LIST extends AionServerPacket {

    byte action;
    Collection<Emotion> emotions;

    /**
     * @param action
     */
    public SM_EMOTION_LIST(byte action, Collection<Emotion> emotions) {
        this.action = action;
        this.emotions = emotions;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(action);
        if (con.getActivePlayer().havePermission(MembershipConfig.EMOTIONS_ALL)) {
            writeH(66);
            for (int i = 0; i < 66; i++) {
                writeH(64 + i);
                writeD(0x00);
            }
        } else if (emotions == null || emotions.isEmpty()) {
            writeH(0);
        } else {
            writeH(emotions.size());
            for (Emotion emotion : emotions) {
                writeH(emotion.getId());
                writeD(emotion.getRemainingTime());// remaining time
            }
        }
    }
}
