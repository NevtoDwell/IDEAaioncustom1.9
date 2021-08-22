/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;

import com.ne.gs.model.gameobjects.Letter;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Mailbox;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.mail.MailMessage;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.MailServicePacket;

/**
 * @author kosyachok
 */
public class SM_MAIL_SERVICE extends MailServicePacket {

    private final int serviceId;
    private Collection<Letter> letters;

    private int mailMessage;

    private Letter letter;
    private long time;

    private int letterId;
    private int[] letterIds;
    private int attachmentType;
    private boolean isExpress;

    public SM_MAIL_SERVICE(Mailbox mailbox) {
        super(null);
        serviceId = 0;
    }

    /**
     * Send mailMessage(ex. Send OK, Mailbox full etc.)
     *
     * @param mailMessage
     */
    public SM_MAIL_SERVICE(MailMessage mailMessage) {
        super(null);
        serviceId = 1;
        this.mailMessage = mailMessage.getId();
    }

    /**
     * Send mailbox info
     *
     * @param player
     * @param letters
     */
    public SM_MAIL_SERVICE(Player player, Collection<Letter> letters) {
        super(player);
        serviceId = 2;
        this.letters = letters;
    }

    public SM_MAIL_SERVICE(Player player, Collection<Letter> letters, boolean isExpress) {
        super(player);
        serviceId = 2;
        this.letters = letters;
        this.isExpress = isExpress;
    }

    /**
     * used when reading letter
     *
     * @param player
     * @param letter
     * @param time
     */
    public SM_MAIL_SERVICE(Player player, Letter letter, long time) {
        super(player);
        serviceId = 3;
        this.letter = letter;
        this.time = time;
    }

    /**
     * used when getting attached items
     *
     * @param letterId
     * @param attachmentType
     */
    public SM_MAIL_SERVICE(int letterId, int attachmentType) {
        super(null);
        serviceId = 5;
        this.letterId = letterId;
        this.attachmentType = attachmentType;
    }

    /**
     * used when deleting letter
     *
     * @param letterIds
     */
    public SM_MAIL_SERVICE(int[] letterIds) {
        super(null);
        serviceId = 6;
        this.letterIds = letterIds;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        Mailbox mailbox = con.getActivePlayer().getMailbox();

        int totalCount = mailbox.size();
        if (totalCount > 90) {
            totalCount = 90;
        }

        int unreadCount = mailbox.getUnreadCount();
        if (unreadCount > totalCount) {
            unreadCount = totalCount;
        }

        int unreadExpressCount = mailbox.getUnreadCountByType(LetterType.EXPRESS);
        if (unreadExpressCount > unreadCount) {
            unreadExpressCount = unreadCount;
        }

        int unreadBlackCloudCount = mailbox.getUnreadCountByType(LetterType.BLACKCLOUD);
        if (unreadBlackCloudCount > unreadCount) {
            unreadBlackCloudCount = unreadCount;
        }

        writeC(serviceId);
        switch (serviceId) {
            case 0:
                mailbox.isMailListUpdateRequired = true;
                writeMailboxState(totalCount, unreadCount, unreadExpressCount, unreadBlackCloudCount);
                break;
            case 1:
                writeMailMessage(mailMessage);
                break;
            case 2:
                writeLettersList(letters, player, isExpress, unreadExpressCount + unreadBlackCloudCount);
                break;
            case 3:
                writeLetterRead(letter, time, totalCount, unreadCount, unreadExpressCount, unreadBlackCloudCount);
                break;
            case 5:
                writeLetterState(letterId, attachmentType);
                break;
            case 6:
                mailbox.isMailListUpdateRequired = true;
                writeLetterDelete(totalCount, unreadCount, unreadExpressCount, unreadBlackCloudCount, letterIds);
                break;
        }
    }
}
