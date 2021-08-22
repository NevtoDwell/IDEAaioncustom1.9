/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LEGION_UPDATE_MEMBER extends AionServerPacket {

    private static final byte OFFLINE = 0x00;
    private static final byte ONLINE = 0x01;
    private final Player player;
    private int msgId;
    private String text;
    private final byte isOnline;

    public SM_LEGION_UPDATE_MEMBER(Player player, int msgId, String text) {
        this.player = player;
        this.msgId = msgId;
        this.text = text;
        isOnline = player.isOnline() ? ONLINE : OFFLINE;
    }

    public SM_LEGION_UPDATE_MEMBER(Player player) {
        this.player = player;
        isOnline = OFFLINE;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(player.getObjectId());
        writeC(player.getLegionMember().getRank().getRankId());
        writeC(player.getCommonData().getPlayerClass().getClassId());
        writeC(player.getLevel());
        writeD(player.getPosition().getMapId());
        writeC(isOnline);
        writeD(player.isOnline() ? 0 : player.getLastOnline());
        writeD(1);
        writeD(msgId);
        writeS(text);
    }
}

// MAP ID: 90 9E 8E 06
// ONLINE: 00
// 00 00 00 00
// D8 74 7C 4B 00 00 4B 00 00

// MAP ID: 90 9E 8E 06
// ONLINE: 01
// 00 00 00 00
// 00 00 00 00

// MAP ID: 90 9E 8E 06
// ONLINE: 01
// 00 00 00 00
// 00 00 00 00
// 00 00

// ONLINE: 01
// UNK: 00 00 00 00
// MEMBER ID: 31 D7 13 00
// MEMBER NAME: 40 00 60 00 60 00 00 00
// but why is it longer?
