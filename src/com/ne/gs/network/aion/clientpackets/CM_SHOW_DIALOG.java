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

/**
 * @author alexa026, Avol modified by ATracer
 */
public class CM_SHOW_DIALOG extends AionClientPacket {

    private int targetObjectId;

    @Override
    protected void readImpl() {
        targetObjectId = readD();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player.isTrading()) {
            return;
        }

        VisibleObject obj = player.getKnownList().getObject(targetObjectId);

        if (obj instanceof Npc) {
            ((Npc) obj).getController().onDialogRequest(player);
        }
    }
}
