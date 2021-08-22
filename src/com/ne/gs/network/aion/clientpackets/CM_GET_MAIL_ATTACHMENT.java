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
import com.ne.gs.services.mail.MailService;

/**
 * @author kosyachok
 */
public class CM_GET_MAIL_ATTACHMENT extends AionClientPacket {

    private int mailObjId;
    private int attachmentType;

    @Override
    protected void readImpl() {
        mailObjId = readD();
        attachmentType = readC(); // 0 - item , 1 - kinah
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        MailService.getInstance().getAttachments(player, mailObjId, attachmentType);
    }
}
