/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.mail.MailService;

/**
 * @author Aion Gates, xTz
 */
public class CM_SEND_MAIL extends AionClientPacket {

    private String recipientName;
    private String title;
    private String message;
    private int itemObjId;
    private int itemCount;
    private int kinahCount;
    private int idLetterType;

    @Override
    protected void readImpl() {
        recipientName = readS();
        title = readS();
        message = readS();
        itemObjId = readD();
        itemCount = readD();
        readD();
        kinahCount = readD();
        readD();
        idLetterType = readC();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (!player.isTrading() && kinahCount < 1000000000 && kinahCount > -1 && itemCount > -2) {
            MailService.getInstance().sendMail(player, recipientName, title, message, itemObjId, itemCount, kinahCount,
                LetterType.getLetterTypeById(idLetterType));
        }
    }
}
