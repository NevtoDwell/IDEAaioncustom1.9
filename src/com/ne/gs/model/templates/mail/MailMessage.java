/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.mail;

/**
 * @author kosyachok
 */
public enum MailMessage {
    MAIL_SEND_SECCESS(0),
    NO_SUCH_CHARACTER_NAME(1),
    RECIPIENT_MAILBOX_FULL(2),
    MAIL_IS_ONE_RACE_ONLY(3),
    YOU_ARE_IN_RECIPIENT_IGNORE_LIST(4),
    RECIPIENT_IGNORING_MAIL_FROM_PLAYERS_LOWER_206_LVL(5),
    // WTF??
    MAILSPAM_WAIT_FOR_SOME_TIME(6);

    private final int id;

    private MailMessage(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
