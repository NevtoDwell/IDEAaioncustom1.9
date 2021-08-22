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
 * @author Sweetkr
 */
public class SM_CUSTOM_SETTINGS extends AionServerPacket {

    private final Integer obj;
    private int unk = 0;
    private final int display;
    private final int deny;

    public SM_CUSTOM_SETTINGS(Player player) {
        this(player.getObjectId(), 1, player.getPlayerSettings().getDisplay(), player.getPlayerSettings().getDeny());
    }

    public SM_CUSTOM_SETTINGS(int objectId, int unk, int display, int deny) {
        obj = objectId;
        this.display = display;
        this.deny = deny;
        this.unk = unk;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(obj);
        writeC(unk); // unk
        writeH(display);
        writeH(deny);
    }
}
