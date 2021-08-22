/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Map;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * Packet with macro list.
 *
 * @author -Nemesiss-
 */
public class SM_MACRO_LIST extends AionServerPacket {
    private final Player player;
    private final boolean secondPart;

    /**
     * Constructs new <tt>SM_MACRO_LIST </tt> packet
     */
    public SM_MACRO_LIST(Player player, boolean secondPart) {
        this.player = player;
        this.secondPart = secondPart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(player.getObjectId());// player id

        Map<Integer, String> macrosToSend = player.getMacroList().getMacrosPart(secondPart);
        int size = macrosToSend.size() * -1;

        writeC(secondPart ? 0x00 : 0x01);
        writeH(size);

        if (size != 0) {
            for (Map.Entry<Integer, String> entry : macrosToSend.entrySet()) {
                writeC(entry.getKey());// order
                writeS(entry.getValue());// xml
            }
        }
    }
}
