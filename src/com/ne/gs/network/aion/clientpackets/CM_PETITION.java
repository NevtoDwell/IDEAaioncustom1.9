/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.Petition;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_PETITION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.PetitionService;

/**
 * @author zdead
 */
public class CM_PETITION extends AionClientPacket {

    private int action;
    private String title = "";
    private String text = "";
    private String additionalData = "";

    @Override
    protected void readImpl() {
        action = readH();
        if (action == 2) {
            readD();
        } else {
            String data = readS();
            String[] dataArr = data.split("/", 3);
            title = dataArr[0];
            text = dataArr[1];
            additionalData = dataArr[2];
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        int playerObjId = player.getObjectId();
        if (action == 2) {
            if (PetitionService.getInstance().hasRegisteredPetition(playerObjId)) {
                int petitionId = PetitionService.getInstance().getPetition(playerObjId).getPetitionId();
                PetitionService.getInstance().deletePetition(playerObjId);
                sendPacket(new SM_SYSTEM_MESSAGE(1300552, petitionId));
                sendPacket(new SM_SYSTEM_MESSAGE(1300553, 49));
                return;
            }
        }

        if (!PetitionService.getInstance().hasRegisteredPetition(playerObjId)) {
            Petition petition = PetitionService.getInstance().registerPetition(player, action, title, text, additionalData);
            sendPacket(new SM_PETITION(petition));
        }
    }
}
