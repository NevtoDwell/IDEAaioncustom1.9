/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.Petition;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.services.PetitionService;

/**
 * @author zdead
 */
public class SM_PETITION extends AionServerPacket {

    private final Petition petition;

    public SM_PETITION() {
        petition = null;
    }

    public SM_PETITION(Petition petition) {
        this.petition = petition;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        if (petition == null) {
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeH(0x00);
            writeC(0x00);
        } else {
            writeC(0x01); // Action ID ?
            writeD(100); // unk (total online players ?)
            writeH(PetitionService.getInstance().getWaitingPlayers(con.getActivePlayer().getObjectId())); // Users
            // waiting for
            // Support
            writeS(Integer.toString(petition.getPetitionId())); // Ticket ID
            writeH(0x00);
            writeC(50); // Total Petitions
            writeC(49); // Remaining Petitions
            writeH(PetitionService.getInstance().calculateWaitTime(petition.getPlayerObjId())); // Estimated minutes
            // before GM reply
            writeD(0x00);
        }
    }
}
