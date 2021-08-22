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
 * This blob entry is sent with ALL items. (unless partial blob is constructed, ie: sending equip slot only) It is the first and only block for non-equipable
 * items, and the last blob for EquipableItems
 *
 * @author -Nemesiss-
 */
public class GeneralInfoBlobEntry extends ItemBlobEntry {

    GeneralInfoBlobEntry() {
        super(ItemBlobType.GENERAL_INFO);
    }

    @Override
    public void writeThisBlob(ByteBuffer buf) {// TODO what with kinah?
        Item item = parent.item;
        writeH(buf, item.getItemMask(parent.player));
        writeQ(buf, item.getItemCount());
        writeS(buf, item.getItemCreator());// Creator name
        writeC(buf, 0);
        writeD(buf, item.getExpireTimeRemaining()); // Disappears time
        writeH(buf, 0);
        writeH(buf, 0);
        writeD(buf, item.getExchangeTime().getRemainingSeconds());
        writeH(buf, 0);
        writeD(buf, 0);
    }
}
