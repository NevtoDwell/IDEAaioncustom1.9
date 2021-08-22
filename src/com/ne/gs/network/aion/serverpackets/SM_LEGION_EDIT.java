/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LEGION_EDIT extends AionServerPacket {

    private final int type;
    private Legion legion;
    private int unixTime;
    private String announcement;

    public SM_LEGION_EDIT(int type) {
        this.type = type;
    }

    public SM_LEGION_EDIT(int type, Legion legion) {
        this.type = type;
        this.legion = legion;
    }

    public SM_LEGION_EDIT(int type, int unixTime) {
        this.type = type;
        this.unixTime = unixTime;
    }

    public SM_LEGION_EDIT(int type, int unixTime, String announcement) {
        this.type = type;
        this.announcement = announcement;
        this.unixTime = unixTime;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(type);
        switch (type) {
            /** Change Legion Level **/
            case 0x00:
                writeC(legion.getLegionLevel());
                break;
            /** Change Legion Rank **/
            case 0x01:
                writeD(legion.getLegionRank());
                break;
            /** Change Legion Permissions **/
            case 0x02:
                writeH(legion.getDeputyPermission());
                writeH(legion.getCenturionPermission());
                writeH(legion.getLegionaryPermission());
                writeH(legion.getVolunteerPermission());
                break;
            /** Change Legion Contributions **/
            case 0x03:
                writeQ(legion.getContributionPoints()); // get Contributions
                break;
            case 0x04:
                writeQ(legion.getLegionWarehouse().getKinah());
                break;
            /** Change Legion Announcement **/
            case 0x05:
                writeS(announcement);
                writeD(unixTime);
                break;
            /** Disband Legion **/
            case 0x06:
                writeD(unixTime);
                break;
            /** Recover Legion **/
            case 0x07:
                break;
            /** Refresh Legion Announcement? **/
            case 0x08:
                break;
        }
    }
}
