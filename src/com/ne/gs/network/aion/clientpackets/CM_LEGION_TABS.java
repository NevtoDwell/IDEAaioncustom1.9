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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team.legion.LegionHistory;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_LEGION_TABS;

/**
 * @author Simple
 */
public class CM_LEGION_TABS extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_LEGION_TABS.class);

    private int page;
    private int tab;

    @Override
    protected void readImpl() {
        page = readD();
        tab = readC();
    }

    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();

        if (activePlayer.getLegion() != null) {
            /**
             * Max page is 16 for legion history
             */
            if (page > 16) {
                return;
            }
            switch (tab) {
                case 0:
                case 2:
                    Collection<LegionHistory> history = activePlayer.getLegion().getLegionHistoryByTabId(tab);

                    if (history.size() < page * 8) {
                        return;
                    }
                    if (!history.isEmpty()) {
                        activePlayer.sendPck(new SM_LEGION_TABS(history, page, tab));
                    }
                    break;
                case 1:
            }

        } else {
            log.warn("Player " + activePlayer.getName() + " was requested null legion");
        }
    }
}
