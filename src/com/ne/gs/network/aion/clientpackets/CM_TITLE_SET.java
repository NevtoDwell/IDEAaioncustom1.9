/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * @author Nemiroff Date: 01.12.2009
 * @modified cura
 */
public class CM_TITLE_SET extends AionClientPacket {

    /**
     * Title id
     */
    private int titleId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        titleId = readH();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (titleId != 0xFFFF) {
            if (!player.getTitleList().contains(titleId) && !player.havePermission(MembershipConfig.TITLES_ADDITIONAL_ENABLE)) {
                return;
            }
        }

        player.getTitleList().setTitle(titleId);
    }
}
