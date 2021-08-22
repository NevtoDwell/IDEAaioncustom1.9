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
 * @author Avol
 */
public class CM_EXCHANGE_ADD_ITEM extends AionClientPacket {

    public int itemObjId;
    public int itemCount;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        itemObjId = readD();
        itemCount = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();
        ExchangeService.getInstance().addItem(activePlayer, itemObjId, itemCount);
    }
}
