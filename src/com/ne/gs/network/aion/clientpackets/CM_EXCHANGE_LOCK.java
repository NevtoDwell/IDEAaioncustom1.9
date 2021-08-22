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
import com.ne.gs.services.ExchangeService;

/**
 * @author -Avol-
 */
public class CM_EXCHANGE_LOCK extends AionClientPacket {

    @Override
    protected void readImpl() {
        // nothing
    }

    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();
        ExchangeService.getInstance().lockExchange(activePlayer);
    }
}
