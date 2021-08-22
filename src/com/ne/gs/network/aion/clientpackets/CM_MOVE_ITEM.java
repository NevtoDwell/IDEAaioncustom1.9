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
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemMoveService;

/**
 * @author alexa026, kosyachok
 */
public class CM_MOVE_ITEM extends AionClientPacket {

    /**
     * Target object id that client wants to TALK WITH or 0 if wants to unselect
     */
    private int targetObjectId;
    private byte source;
    private byte destination;
    private short slot;

    @Override
    protected void readImpl() {
        targetObjectId = readD();// empty
        source = readSC(); // FROM (0 - player inventory, 1 - regular warehouse, 2 - account warehouse, 3 - legion
        // warehouse)
        destination = readSC(); // TO
        slot = readSH();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        ItemMoveService.moveItem(player, targetObjectId, source, destination, slot);
    }
}
