/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.commons.utils.Rnd;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author Rhys2002
 */
public class CM_CLIENT_COMMAND_ROLL extends AionClientPacket {

    private int maxRoll;
    private int roll;

    @Override
    protected void readImpl() {
        maxRoll = readD();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        roll = Rnd.get(1, maxRoll);
        player.sendPck(new SM_SYSTEM_MESSAGE(1400126, roll, maxRoll));
        PacketSendUtility.broadcastPacket(player, new SM_SYSTEM_MESSAGE(1400127, player.getName(), roll, maxRoll));
    }
}
