/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ne.gs.model.AbyssRankingResult;
import com.ne.gs.model.Race;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author zdead, LokiReborn
 */
public class SM_ABYSS_RANKING_LEGIONS extends AionServerPacket {

    private final List<AbyssRankingResult> data;
    private final Race race;
    private final int updateTime;
    private int sendData = 0;

    public SM_ABYSS_RANKING_LEGIONS(int updateTime, ArrayList<AbyssRankingResult> data, Race race) {
        this.updateTime = updateTime;
        this.data = data;
        this.race = race;
        sendData = 1;
    }

    public SM_ABYSS_RANKING_LEGIONS(int updateTime, Race race) {
        this.updateTime = updateTime;
        data = Collections.emptyList();
        this.race = race;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(race.getRaceId());// 0:Elyos 1:Asmo
        writeD(updateTime);// Date
        writeD(sendData);// 0:Nothing 1:Update Table
        writeD(sendData);// 0:Nothing 1:Update Table
        writeH(data.size());// list size
        for (AbyssRankingResult rs : data) {
            writeD(rs.getRankPos());// Current Rank
            writeD((rs.getOldRankPos() == 0) ? 76 : rs.getOldRankPos());// Old Rank
            writeD(rs.getLegionId());// Legion Id
            writeD(race.getRaceId());// 0:Elyos 1:Asmo
            writeC(rs.getLegionLevel());// Legion Level
            writeD(rs.getLegionMembers());// Legion Members
            writeQ(rs.getLegionCP());// Contribution Points
            writeS(rs.getLegionName(), 82);// Legion Name
        }
    }
}
