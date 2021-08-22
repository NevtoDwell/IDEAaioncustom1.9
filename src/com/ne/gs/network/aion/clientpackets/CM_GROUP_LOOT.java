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
import com.ne.gs.services.drop.DropDistributionService;

/**
 * @author Rhys2002
 */
public class CM_GROUP_LOOT extends AionClientPacket {

    @SuppressWarnings("unused")
    private int groupId;
    private int index;
    @SuppressWarnings("unused")
    private int unk1;
    private int itemId;
    private int unk2;
    private int unk3;
    private int npcId;
    private int distributionId;
    private int roll;
    private long bid;
    private int unk4;

    @Override
    protected void readImpl() {
        groupId = readD();
        index = readD();
        unk1 = readD();
        itemId = readD();
        unk2 = readC();
        unk3 = readC();
        npcId = readD();
        distributionId = readC();// 2: Roll 3: Bid
        roll = readD();// 0: Never Rolled 1: Rolled
        bid = readD();// 0: No Bid else bid amount
        unk4 = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player == null) {
            return;
        }
        switch (distributionId) {
            case 2:
                DropDistributionService.getInstance().handleRoll(player, roll, itemId, npcId, index);
                break;
            case 3:
                DropDistributionService.getInstance().handleBid(player, bid, itemId, npcId, index);
                break;
        }
    }
}
