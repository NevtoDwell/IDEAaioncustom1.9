/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.player.AbyssRank;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.utils.stats.AbyssRankEnum;

/**
 * @author Nemiroff Date: 25.01.2010
 */
public class SM_ABYSS_RANK extends AionServerPacket {

    private final AbyssRank rank;
    private final int currentRankId;

    public SM_ABYSS_RANK(AbyssRank rank) {
        this.rank = rank;
        currentRankId = rank.getRank().getId();
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeQ(rank.getAp()); // curAP
        writeD(currentRankId); // curRank
        writeD(rank.getTopRanking()); // curRating

        int nextRankId = currentRankId < AbyssRankEnum.values().length ? currentRankId + 1 : currentRankId;
        writeD(100 * rank.getAp() / AbyssRankEnum.getRankById(nextRankId).getRequired()); // exp %

        writeD(rank.getAllKill()); // allKill
        writeD(rank.getMaxRank()); // maxRank

        writeD(rank.getDailyKill()); // dayKill
        writeQ(rank.getDailyAP()); // dayAP

        writeD(rank.getWeeklyKill()); // weekKill
        writeQ(rank.getWeeklyAP()); // weekAP

        writeD(rank.getLastKill()); // laterKill
        writeQ(rank.getLastAP()); // laterAP

        writeC(0x00); // unk
    }
}
