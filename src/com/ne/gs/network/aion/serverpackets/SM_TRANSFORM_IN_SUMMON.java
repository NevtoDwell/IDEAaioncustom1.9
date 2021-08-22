/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_TRANSFORM_IN_SUMMON extends AionServerPacket {

    private final Player player;
    private final int summonObject;

    public SM_TRANSFORM_IN_SUMMON(Player player, Creature creature) {
        this(player, creature.getObjectId());
    }

    public SM_TRANSFORM_IN_SUMMON(Player player, int creatureObjectId) {
        this.player = player;
        summonObject = creatureObjectId;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(summonObject);
        writeS(player.getName());
        writeD(player.getObjectId());
    }
}
