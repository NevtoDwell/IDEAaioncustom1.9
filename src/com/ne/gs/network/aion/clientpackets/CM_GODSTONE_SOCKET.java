/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.item.ItemSocketService;
import com.ne.gs.utils.MathUtil;

/**
 * @author ATracer
 */
public class CM_GODSTONE_SOCKET extends AionClientPacket {

    private int npcObjectId;
    private int weaponId;
    private int stoneId;

    @Override
    protected void readImpl() {
        npcObjectId = readD();
        weaponId = readD();
        stoneId = readD();
    }

    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();

        VisibleObject obj = activePlayer.getKnownList().getObject(npcObjectId);
        if (obj != null && obj instanceof Npc && MathUtil.isInRange(activePlayer, obj, 7)) {
            ItemSocketService.socketGodstone(activePlayer, weaponId, stoneId);
        }
    }
}
