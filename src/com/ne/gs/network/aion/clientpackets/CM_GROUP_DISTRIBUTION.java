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
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.restrictions.RestrictionsManager;

/**
 * @author Lyahim, Simple, xTz
 */
public class CM_GROUP_DISTRIBUTION extends AionClientPacket {

    private long amount;
    private int partyType;

    @Override
    protected void readImpl() {
        amount = readQ();
        partyType = readC();
    }

    @Override
    protected void runImpl() {
        if (amount < 2) {
            return;
        }

        Player player = getConnection().getActivePlayer();

        if (!RestrictionsManager.canTrade(player)) {
            return;
        }

        switch (partyType) {
            case 1:
                if (player.isInAlliance2()) {
                    PlayerAllianceService.distributeKinahInGroup(player, amount);
                } else {
                    PlayerGroupService.distributeKinah(player, amount);
                }
                break;
            case 2:
                PlayerAllianceService.distributeKinah(player, amount);
                break;
        }
    }
}
