/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.serverpackets.SM_HEADING_UPDATE;
import com.ne.gs.services.DialogService;
import com.ne.gs.utils.ThreadPoolManager;

public class CM_CLOSE_DIALOG extends AionClientPacket {

    /**
     * Target object id that client wants to TALK WITH or 0 if wants to unselect
     */
    private int targetObjectId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        targetObjectId = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        final VisibleObject obj = player.getKnownList().getObject(targetObjectId);
        final AionConnection client = getConnection();
        if (obj == null) {
            return;
        }

        if (obj instanceof Npc) {
            Npc npc = (Npc) obj;
            npc.getAi2().onCreatureEvent(AIEventType.DIALOG_FINISH, player);
            DialogService.onCloseDialog(npc, player);
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    client.sendPacket(new SM_HEADING_UPDATE(targetObjectId, obj.getHeading()));
                }
            }, 1200);
        }
        if (player.getMailbox().mailBoxState != 0) {
            player.getMailbox().mailBoxState = 0;
        }
    }
}
