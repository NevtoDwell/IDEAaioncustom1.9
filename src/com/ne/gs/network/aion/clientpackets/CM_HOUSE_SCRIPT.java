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

public class CM_HOUSE_SCRIPT extends AionClientPacket {

    int address;
    int scriptIndex;
    int totalSize;
    int compressedSize;
    int uncompressedSize;
    byte[] stream;

    @Override
    protected void readImpl() {
        address = readD();
        scriptIndex = readC();
        totalSize = readH();
        if (totalSize > 0) {
            compressedSize = readD();
            if (compressedSize < 8150) {
                uncompressedSize = readD();
                stream = readB(compressedSize);
            }
        }
    }

    @Override
    protected void runImpl() {
        // TODO
        //		final Player player = getConnection().getActivePlayer();
        //
        //		if (compressedSize > 8149) {
        //			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_SCRIPT_OVERFLOW);
        //		}
        //
        //		if (!HouseOwner.of(player).hasHouse()) return;
        //
        //		final PlayerScripts scripts = new PlayerScripts(1); // FIXME housing house.getPlayerScripts();
        //
        //		if (totalSize <= 0) {
        //			scripts.addScript(scriptIndex, new byte[0], 0);
        //		} else {
        //			scripts.addScript(scriptIndex, stream, uncompressedSize);
        //		}
        //
        //		PacketSendUtility.sendPacket(player, new SM_HOUSE_SCRIPTS(address, scripts, scriptIndex, scriptIndex));
    }
}
