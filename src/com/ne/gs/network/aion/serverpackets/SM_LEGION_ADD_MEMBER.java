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
public class SM_LEGION_ADD_MEMBER extends AionServerPacket {

    private final Player player;
    private final boolean isMember;
    private final int msgId;
    private final String text;

    public SM_LEGION_ADD_MEMBER(Player player, boolean isMember, int msgId, String text) {
        this.player = player;
        this.isMember = isMember;
        this.msgId = msgId;
        this.text = text;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(player.getObjectId());
        writeS(player.getName());
        writeC(player.getLegionMember().getRank().getRankId());
        writeC(isMember ? 0x01 : 0x00);// is New Member?
        writeC(player.getCommonData().getPlayerClass().getClassId());
        writeC(player.getLevel());
        writeD(player.getPosition().getMapId());
        writeD(1);
        writeD(msgId);
        writeS(text);
    }
}
