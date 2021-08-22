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
import com.ne.gs.world.WorldMapType;

/**
 * This packet is notify client what map should be loaded.
 *
 * @author -Nemesiss-
 */
public class SM_PLAYER_SPAWN extends AionServerPacket {

    /**
     * Player that is entering game.
     */
    private final Player player;

    /**
     * Constructor.
     *
     * @param player
     */
    public SM_PLAYER_SPAWN(Player player) {
        super();
        this.player = player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(player.getWorldId());
        writeD(player.getWorldId());// world + chnl
        writeD(0x00);// unk
        writeC(WorldMapType.of(player.getWorldId()).isPersonal() ? 1 : 0);
        writeF(player.getX());// x
        writeF(player.getY());// y
        writeF(player.getZ());// z
        writeC(player.getHeading());// heading
        writeD(0); // new 2.5
        writeD(0); // new 2.5
        writeD(0); // new 2.7
        writeC(0);
    }
}
