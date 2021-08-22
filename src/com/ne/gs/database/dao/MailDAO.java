/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.sql.Timestamp;

import com.ne.gs.model.gameobjects.Letter;
import com.ne.gs.model.gameobjects.player.Mailbox;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;

/**
 * @author kosyachok
 */
public abstract class MailDAO implements IDFactoryAwareDAO {

    @Override
    public String getClassName() {
        return MailDAO.class.getName();
    }

    public abstract boolean storeLetter(Timestamp time, Letter letter);

    public abstract Mailbox loadPlayerMailbox(Player player);

    public abstract void storeMailbox(Player player);

    public abstract boolean deleteLetter(int letterId);

    public abstract void updateOfflineMailCounter(PlayerCommonData recipientCommonData);

    public abstract boolean haveUnread(int playerId);
}
