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
import com.ne.gs.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author Sweetkr
 */
public class CM_CUSTOM_SETTINGS extends AionClientPacket {

    private int display;
    private int deny;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        /**
         * 1 : show legion mantle 2 : priority equipment 4 : show helmet
         */
        display = readH();
        /**
         * 1 : view detail player 2 : trade 4 : party/force 8 : legion 16 : friend 32 : dual(pvp)
         */
        deny = readH();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();
        activePlayer.getPlayerSettings().setDisplay(display);
        activePlayer.getPlayerSettings().setDeny(deny);

        PacketSendUtility.broadcastPacket(activePlayer, new SM_CUSTOM_SETTINGS(activePlayer), true);
    }
}
