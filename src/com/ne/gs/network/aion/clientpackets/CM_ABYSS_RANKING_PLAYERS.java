/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.AbyssRank.AbyssRankUpdateType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_ABYSS_RANKING_PLAYERS;
import com.ne.gs.services.abyss.AbyssRankingCache;

/**
 * @author SheppeR
 */
public class CM_ABYSS_RANKING_PLAYERS extends AionClientPacket {

    private Race queriedRace;
    private int raceId;
    private AbyssRankUpdateType updateType;

    private static final Logger log = LoggerFactory.getLogger(CM_ABYSS_RANKING_PLAYERS.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        raceId = readC();
        switch (raceId) {
            case 0:
                queriedRace = Race.ELYOS;
                updateType = AbyssRankUpdateType.PLAYER_ELYOS;
                break;
            case 1:
                queriedRace = Race.ASMODIANS;
                updateType = AbyssRankUpdateType.PLAYER_ASMODIANS;
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        if (queriedRace != null) {
            Player player = getConnection().getActivePlayer();
            if (player.isAbyssRankListUpdated(updateType)) {
                sendPacket(new SM_ABYSS_RANKING_PLAYERS(AbyssRankingCache.getInstance().getLastUpdate(), queriedRace));
            } else {
                List<SM_ABYSS_RANKING_PLAYERS> results = AbyssRankingCache.getInstance().getPlayers(queriedRace);
                for (SM_ABYSS_RANKING_PLAYERS packet : results) {
                    sendPacket(packet);
                }
                player.setAbyssRankListUpdated(updateType);
            }
        } else {
            log.warn("Received invalid raceId: " + raceId);
        }
    }
}
