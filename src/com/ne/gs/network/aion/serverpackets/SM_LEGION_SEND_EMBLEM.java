/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.team.legion.LegionEmblemType;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Simple modified cura
 */
public class SM_LEGION_SEND_EMBLEM extends AionServerPacket {

    /**
     * Legion information *
     */
    private final int legionId;
    private final int emblemId;
    private final int color_r;
    private final int color_g;
    private final int color_b;
    private final String legionName;
    private final LegionEmblemType emblemType;
    private final int emblemDataSize;

    /**
     * This constructor will handle legion emblem info
     *
     * @param legionId
     * @param emblemId
     * @param color_r
     * @param color_g
     * @param color_b
     * @param legionName
     * @param emblemType
     * @param emblemDataSize
     */
    public SM_LEGION_SEND_EMBLEM(int legionId, int emblemId, int color_r, int color_g, int color_b, String legionName,
                                 LegionEmblemType emblemType, int emblemDataSize) {
        this.legionId = legionId;
        this.emblemId = emblemId;
        this.color_r = color_r;
        this.color_g = color_g;
        this.color_b = color_b;
        this.legionName = legionName;
        this.emblemType = emblemType;
        this.emblemDataSize = emblemDataSize;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(legionId);
        writeC(emblemId);
        writeC(emblemType.getValue());
        writeD(emblemDataSize);
        writeC(emblemType.equals(LegionEmblemType.DEFAULT) ? 0x00 : 0xFF);
        writeC(color_r);
        writeC(color_g);
        writeC(color_b);
        writeS(legionName);
        writeC(0x01);
    }
}
