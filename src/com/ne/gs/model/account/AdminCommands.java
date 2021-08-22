/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.account;

/**
 * Created with IntelliJ IDEA.
 * User: Alexsis
 * Date: 14.12.12
 * Time: 20:09
 * To change this template use File | Settings | File Templates.
 */
public enum AdminCommands {

    gm_mail_list(0),
    inventory(1),
    skill(2),
    teleportto(3),
    status(4),
    search(5),
    quest(6),
    gm_guildhistory(7),
    gm_buddy_list(8),
    recall(9),
    gm_comment_list(10),
    gm_comment_add(11),
    check_bot1(12),
    check_bot99(13),
    Bookmark_add(14),
    guild(15);

    private final int value;

    private AdminCommands(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
