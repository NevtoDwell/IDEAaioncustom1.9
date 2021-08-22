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
 * This block is sent for all items that can be equipped. If item is equipped. This block says to which slot it's equipped. If not, then it says 0.
 *
 * @author -Nemesiss-
 */
public class EquippedSlotBlobEntry extends ItemBlobEntry {

    EquippedSlotBlobEntry() {
        super(ItemBlobType.EQUIPPED_SLOT);
    }

    @Override
    public void writeThisBlob(ByteBuffer buf) {
        Item item = parent.item;

        writeD(buf, item.isEquipped() ? item.getEquipmentSlot() : 0x00);
    }
}
