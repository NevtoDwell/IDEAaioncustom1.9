/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;

import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.model.AbyssRankingResult;
import com.ne.gs.model.Race;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Rhys2002, zdead, LokiReborn
 */
public class SM_ABYSS_RANKING_PLAYERS extends AionServerPacket {

    private final List<AbyssRankingResult> data;
    private final int lastUpdate;
    private final int race;
    private final int page;
    private final boolean isEndPacket;

    public SM_ABYSS_RANKING_PLAYERS(int lastUpdate, List<AbyssRankingResult> data, Race race, int page, boolean isEndPacket) {
        this.lastUpdate = lastUpdate;
        this.data = data;
        this.race = race.getRaceId();
        this.page = page;
        this.isEndPacket = isEndPacket;
    }

    public SM_ABYSS_RANKING_PLAYERS(int lastUpdate, Race race) {
        this.lastUpdate = lastUpdate;
        data = Collections.emptyList();
        this.race = race.getRaceId();
        page = 0;
        isEndPacket = false;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(race);// 0:Elyos 1:Asmo
        writeD(lastUpdate);// Date
        writeD(page);
        writeD(isEndPacket ? 0x7F : 0);// 0:Nothing 1:Update Table
        writeH(data.size());// list size

        for (AbyssRankingResult rs : data) {
            writeD(rs.getRankPos());// Current Rank
            writeD(rs.getPlayerAbyssRank());// Abyss rank
            writeD((rs.getOldRankPos() == 0) ? 501 : rs.getOldRankPos());// Old Rank
            writeD(rs.getPlayerId()); // PlayerID
            writeD(race);
            writeD(rs.getPlayerClass().getClassId());// Class Id
            writeD(0); // Sex ? 0=male / 1=female
            writeQ(rs.getPlayerAP());// Abyss Points
            writeH(rs.getPlayerLevel());

            writeS(rs.getPlayerName(), 52);// Player Name
            writeS(rs.getLegionName(), 82);// Legion Name
            if (GSConfig.SERVER_VERSION.startsWith("3.1")) {
                writeD(0);
            }
        }
    }
}
