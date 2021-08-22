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
 * @author IlBuono
 */
public class SM_PLASTIC_SURGERY extends AionServerPacket {

    private final int playerObjId;
    private final byte check_ticket;
    private final byte change_sex;

    public SM_PLASTIC_SURGERY(Player player, byte check_ticket, byte change_sex) {
        playerObjId = player.getObjectId();
        this.check_ticket = check_ticket;
        this.change_sex = change_sex;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playerObjId);
        writeC(check_ticket);
        writeC(change_sex);
    }
}
