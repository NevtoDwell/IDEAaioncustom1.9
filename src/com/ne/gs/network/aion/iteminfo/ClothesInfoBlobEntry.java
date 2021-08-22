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
import com.ne.gs.model.items.ItemSlot;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob is sent for clothes. It keeps info about slots that cloth can be equipped to.
 *
 * @author -Nemesiss-
 */
public class ClothesInfoBlobEntry extends ItemBlobEntry {

    ClothesInfoBlobEntry() {
        super(ItemBlobType.SLOTS_CLOTHES);
    }

    @Override
    public void writeThisBlob(ByteBuffer buf) {
        Item item = parent.item;

        writeD(buf, ItemSlot.getSlotFor(item.getItemTemplate().getItemSlot()).id());
        writeD(buf, 0);// TODO! secondary slot?
    }
}
