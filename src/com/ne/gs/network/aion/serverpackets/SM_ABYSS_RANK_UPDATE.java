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
 * @author Nemiroff Date: 17.02.2010
 */
// TODO Rename
public class SM_ABYSS_RANK_UPDATE extends AionServerPacket {

    private final Player player;
    private final int action;

    public SM_ABYSS_RANK_UPDATE(int action, Player player) {
        this.action = action;
        this.player = player;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(action);
        writeD(player.getObjectId());
        switch (action) {
            case 0:
                writeD(player.getAbyssRank().getRank().getId());
                break;
            case 1:
                if (player.isMentor()) {
                    writeD(1);
                } else {
                    writeD(0);
                }
                break;
            case 2:
                if (player.isMentor()) {
                    writeD(1);
                } else {
                    writeD(0);
                }
                break;
        }
    }
}
