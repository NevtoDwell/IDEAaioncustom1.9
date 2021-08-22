/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob sends info about conditioning.
 *
 * @author -Nemesiss-
 */
public class ConditioningInfoBlobEntry extends ItemBlobEntry {

    ConditioningInfoBlobEntry() {
        super(ItemBlobType.CONDITIONING_INFO);
    }

    @Override
    public void writeThisBlob(ByteBuffer buf) {
        Item item = parent.item;

        writeD(buf, item.getChargePoints());
    }
}
