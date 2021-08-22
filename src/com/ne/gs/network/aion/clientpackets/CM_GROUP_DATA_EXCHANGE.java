/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import java.util.Collection;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_DATA_EXCHANGE;

public class CM_GROUP_DATA_EXCHANGE extends AionClientPacket {

    private int groupType;
    private int action;
    private int unk2;
    private int dataSize;
    private byte[] data;

    @Override
    protected void readImpl() {
        action = readC();

        switch (action) {
            case 1:
                dataSize = readD();
                break;
            default:
                groupType = readC();
                unk2 = readC();
                dataSize = readD();
        }

        if ((dataSize > 0) && (dataSize <= 5086)) {
            data = readB(dataSize);
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if ((player == null) || (data == null)) {
            return;
        }

        if (action == 1) {
            player.sendPck(new SM_GROUP_DATA_EXCHANGE(data));
            return;
        }

        Collection<Player> players = null;
        switch (groupType) {
            case 0:
                if (player.isInGroup2()) {
                    players = player.getPlayerGroup2().getOnlineMembers();
                }
                break;
            case 1:
                if (player.isInAlliance2()) {
                    players = player.getPlayerAllianceGroup2().getOnlineMembers();
                }
                break;
            case 2:
                if (player.isInLeague()) {
                    players = player.getPlayerAllianceGroup2().getOnlineMembers();
                }
                break;
        }

        if (players != null) {
            for (Player member : players) {
                if (!member.equals(player)) {
                    member.sendPck(new SM_GROUP_DATA_EXCHANGE(data, action, unk2));
                }
            }
        }
    }
}
