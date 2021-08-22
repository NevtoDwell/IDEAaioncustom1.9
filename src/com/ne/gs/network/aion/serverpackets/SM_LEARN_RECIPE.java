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
 * @author lord_rex
 */
public class SM_LEARN_RECIPE extends AionServerPacket {

    private final int recipeId;

    public SM_LEARN_RECIPE(int recipeId) {
        this.recipeId = recipeId;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(recipeId);
    }
}
