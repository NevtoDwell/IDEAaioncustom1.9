/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.world.World;

/**
 * @author alexa026
 */
public class SM_DIALOG_WINDOW extends AionServerPacket {

    private final int targetObjectId;
    private final int dialogID;
    private int questId = 0;

    public SM_DIALOG_WINDOW(int targetObjectId, int dlgID) {
        this.targetObjectId = targetObjectId;
        dialogID = dlgID;
    }

    public SM_DIALOG_WINDOW(int targetObjectId, int dlgID, int questId) {
        this.targetObjectId = targetObjectId;
        dialogID = dlgID;
        this.questId = questId;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected void writeImpl(AionConnection con) {
        Player player = con.getActivePlayer();
        writeD(targetObjectId);
        writeH(dialogID);
        writeD(questId);
        writeH(0);
        if (dialogID == 18) {
            AionObject object = World.getInstance().findVisibleObject(targetObjectId);
            if (object != null && object instanceof Npc) {
                Npc znpc = (Npc) object;
                if (znpc.getNpcId() == 798100 || znpc.getNpcId() == 798101) {
                    player.getMailbox().mailBoxState = 2;
                    writeH(2);
                } else {
                    player.getMailbox().mailBoxState = 1;
                }
            } else {
                writeH(0);
            }
        } else {
            writeH(0);
        }
    }
}
