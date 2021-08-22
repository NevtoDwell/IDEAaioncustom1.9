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

import com.ne.gs.network.PacketWriteHelper;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * ItemInfo blob entry (contains detailed item info).
 *
 * @author -Nemesiss-
 */
public abstract class ItemBlobEntry extends PacketWriteHelper {

    private final ItemBlobType type;
    private ItemBlobEntry nextBlob;
    ItemInfoBlob parent;

    ItemBlobEntry(ItemBlobType type) {
        this.type = type;
    }

    void setParent(ItemInfoBlob parent) {
        this.parent = parent;
    }

    void addBlobEntry(ItemBlobEntry ent) {
        if (nextBlob == null) {
            nextBlob = ent;
        } else {
            nextBlob.addBlobEntry(ent);
        }
    }

    @Override
    protected void writeMe(ByteBuffer buf) {
        writeC(buf, type.getEntryId());
        writeThisBlob(buf);
        if (nextBlob != null) {
            nextBlob.writeMe(buf);
        }
    }

    public abstract void writeThisBlob(ByteBuffer buf);
}
