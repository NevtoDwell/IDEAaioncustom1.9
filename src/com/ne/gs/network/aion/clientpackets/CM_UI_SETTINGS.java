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

/**
 * @author ATracer
 */
public class CM_UI_SETTINGS extends AionClientPacket {

    int settingsType;
    byte[] data;
    int size;

    @Override
    protected void readImpl() {
        settingsType = readC();
        readH();
        size = readH();
        data = readB(getRemainingBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (settingsType == 0) {
            player.getPlayerSettings().setUiSettings(data);
        } else if (settingsType == 1) {
            player.getPlayerSettings().setShortcuts(data);
        }
    }
}
