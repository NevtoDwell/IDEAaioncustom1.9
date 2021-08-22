/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.sql.Timestamp;
import java.util.Map;

import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LEGION_INFO extends AionServerPacket {

    /**
     * Legion information *
     */
    private final Legion legion;

    /**
     * This constructor will handle legion info
     *
     * @param legion
     */
    public SM_LEGION_INFO(Legion legion) {
        this.legion = legion;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeS(legion.getLegionName());
        writeC(legion.getLegionLevel());
        writeD(legion.getLegionRank());
        writeH(legion.getDeputyPermission());
        writeH(legion.getCenturionPermission());
        writeH(legion.getLegionaryPermission());
        writeH(legion.getVolunteerPermission());
        writeQ(legion.getContributionPoints());
        writeD(0x00); // unk
        writeD(0x00); // unk
        writeD(0x00); // unk
        /**
         * Get Announcements List From DB By Legion *
         */
        Map<Timestamp, String> announcementList = legion.getAnnouncementList().descendingMap();

        /**
         * Show max 7 announcements *
         */
        int i = 0;
        for (Timestamp unixTime : announcementList.keySet()) {
            writeS(announcementList.get(unixTime));
            writeD((int) (unixTime.getTime() / 1000));
            i++;
            if (i >= 7) {
                break;
            }
        }
        writeH(0x00);
    }
}
