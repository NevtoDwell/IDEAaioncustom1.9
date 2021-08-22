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

/**
 * @author Sweetkr
 * @author Simple
 */
public class CM_SHOW_BRAND extends AionClientPacket {

    @SuppressWarnings("unused")
    private int action;
    private int brandId;
    private int targetObjectId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        action = readD();
        brandId = readD();
        targetObjectId = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (player.isInGroup2() && player.getPlayerGroup2().isLeader(player)) {
            PlayerGroupService.showBrand(player, targetObjectId, brandId);
        }
        if (player.isInAlliance2() && player.getPlayerAlliance2().isSomeCaptain(player)) {
            PlayerAllianceService.showBrand(player, targetObjectId, brandId);
        }
    }
}
