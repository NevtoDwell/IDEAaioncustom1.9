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
import com.ne.gs.services.StaticDoorService;

/**
 * @author rhys2002 & Wakizashi
 */
public class CM_OPEN_STATICDOOR extends AionClientPacket {

    private int doorId;

    @Override
    protected void readImpl() {
        doorId = readD();
    }

    @Override
    protected void runImpl() {
        Player player = this.getConnection().getActivePlayer();
        StaticDoorService.getInstance().openStaticDoor(player, doorId);
    }

}
