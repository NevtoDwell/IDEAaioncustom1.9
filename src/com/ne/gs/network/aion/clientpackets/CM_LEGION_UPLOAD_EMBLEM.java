/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.LegionService;

/**
 * @author Simple
 */
public class CM_LEGION_UPLOAD_EMBLEM extends AionClientPacket {

    /**
     * Emblem related information *
     */
    private int size;
    private byte[] data;

    @Override
    protected void readImpl() {
        size = readD();
        data = new byte[size];
        data = readB(size);
    }

    @Override
    protected void runImpl() {
        if (data != null && data.length > 0) {
            LegionService.getInstance().uploadEmblemData(getConnection().getActivePlayer(), size, data);
        }
    }
}
