/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.player.BlockList;
import com.ne.gs.model.gameobjects.player.BlockedPlayer;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * Packet responsible for telling a player his block list
 *
 * @author Ben
 */
public class SM_BLOCK_LIST extends AionServerPacket {

    @Override
    protected void writeImpl(AionConnection con) {
        BlockList list = con.getActivePlayer().getBlockList();
        writeH(list.getSize());
        writeC(0); // Unk
        for (BlockedPlayer player : list) {
            writeS(player.getName());
            writeS(player.getReason());
        }
    }
}
