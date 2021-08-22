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
import java.util.Set;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.items.ManaStone;
import com.ne.gs.model.stats.calc.functions.StatFunction;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob is sending info about the item that were fused with current item.
 *
 * @author -Nemesiss-
 */
public class CompositeItemBlobEntry extends ItemBlobEntry {

    CompositeItemBlobEntry() {
        super(ItemBlobType.COMPOSITE_ITEM);
    }

    @Override
    public void writeThisBlob(ByteBuffer buf) {
        Item item = parent.item;

        writeD(buf, item.getFusionedItemId());
        writeFusionStones(buf);
        writeH(buf, item.hasOptionalFusionSocket() ? item.getOptionalFusionSocket() : 0x00);
        writeH(buf, 6);
        writeH(buf, 0);
        writeH(buf, 256);
        writeH(buf, 3);
        writeH(buf, 0);
        writeH(buf, 0);
        writeH(buf, 0);
    }

    private void writeFusionStones(ByteBuffer buf) {
        Item item = parent.item;
        int count = 0;

        if (item.hasFusionStones()) {
            Set<ManaStone> itemStones = item.getFusionStones();

            for (ManaStone itemStone : itemStones) {
                if (count == 6) {
                    break;
                }

                StatFunction modifier = itemStone.getFirstModifier();
                if (modifier != null) {
                    count++;
                    writeH(buf, modifier.getName().getItemStoneMask());
                    writeH(buf, modifier.getValue());
                }
            }
            skip(buf, (6 - count) * 4);
        } else {
            skip(buf, 24);
        }
    }
}
