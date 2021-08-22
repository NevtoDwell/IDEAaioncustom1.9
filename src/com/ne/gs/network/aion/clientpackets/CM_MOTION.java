/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * @author MrPoke
 */
public class CM_MOTION extends AionClientPacket {

    private int motionId;
    private int motionType;

    @Override
    protected void readImpl() {
        readC(); // unk 4
        motionId = readH();
        motionType = readC();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        player.getMotions().setActive(motionId, motionType);
    }
}
