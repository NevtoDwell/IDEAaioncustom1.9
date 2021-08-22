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
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionConnection.State;
import com.ne.gs.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.services.player.PlayerLeaveWorldService;

/**
 * In this packets aion client is asking if may quit.
 *
 * @author -Nemesiss-
 */
public class CM_QUIT extends AionClientPacket {

    /**
     * Logout - if true player is wanted to go to character selection.
     */
    private boolean logout;

    @Override
    protected void readImpl() {
        logout = readC() == 1;
    }

    @Override
    protected void runImpl() {
        AionConnection client = getConnection();

        Player player = null;
        if (client.getState() == State.IN_GAME) {
            player = client.getActivePlayer();
            // TODO! check if may quit
            if (!logout) {
                LoginServer.getInstance().aionClientDisconnected(client.getAccount().getId());
            }
            PlayerLeaveWorldService.startLeaveWorld(player);
            client.setActivePlayer(null);
        }

        if (logout) {
            if (player != null && player.isInEditMode()) {
                sendPacket(new SM_QUIT_RESPONSE(true));
                player.setEditMode(false);
            } else {
                sendPacket(new SM_QUIT_RESPONSE());
            }
        } else {
            client.close(new SM_QUIT_RESPONSE(), false);
        }
    }
}
