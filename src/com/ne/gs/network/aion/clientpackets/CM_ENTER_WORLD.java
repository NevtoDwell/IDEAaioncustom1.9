/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.services.player.PlayerEnterWorldService;

/**
 * In this packets aion client is asking if given char [by oid] may login into game [ie start playing].
 *
 * @author -Nemesiss-, Avol
 */
public class CM_ENTER_WORLD extends AionClientPacket {

    /**
     * Object Id of player that is entering world
     */
    private int objectId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        objectId = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {

        AionConnection client = getConnection();
        PlayerEnterWorldService.startEnterWorld(objectId, client);
    }
}
