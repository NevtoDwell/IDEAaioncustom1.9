/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_SHOW_BRAND extends AionServerPacket {

    private final int brandId;
    private final int targetObjectId;

    public SM_SHOW_BRAND(int brandId, int targetObjectId) {
        this.brandId = brandId;
        this.targetObjectId = targetObjectId;
    }

    @Override
    protected void writeImpl(AionConnection con) {

        writeH(0x01);
        writeD(0x01); // unk
        writeD(brandId);
        writeD(targetObjectId);

    }
}
