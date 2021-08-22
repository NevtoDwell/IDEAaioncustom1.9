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
import com.ne.gs.model.items.ItemStone;
import com.ne.gs.model.items.ManaStone;
import com.ne.gs.model.stats.calc.functions.StatFunction;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob sends info about mana stones.
 *
 * @author -Nemesiss-
 */
public class ManaStoneInfoBlobEntry extends ItemBlobEntry {

    ManaStoneInfoBlobEntry() {
        super(ItemBlobType.MANA_SOCKETS);
    }

    @Override
    public void writeThisBlob(ByteBuffer buf) {
        Item item = parent.item;

        writeC(buf, item.isSoulBound() ? 1 : 0);
        writeC(buf, item.getEnchantLevel()); // enchant (1-15)
        writeD(buf, item.getItemSkinTemplate().getTemplateId());
        writeC(buf, item.getOptionalSocket());

        writeItemStones(buf);

        ItemStone god = item.getGodStone();
        writeD(buf, god == null ? 0 : god.getItemId());

        writeD(buf, item.getItemColor());

        writeD(buf, 0);// unk 1.5.1.9
        writeD(buf, 0);// unk 2.7
        writeC(buf, 0);// unk
    }

    /**
     * Writes manastones : 6C - statenum mask, 6H - value
     *
     */
    private void writeItemStones(ByteBuffer buf) {
        Item item = parent.item;
        int count = 0;

        if (item.hasManaStones()) {
            Set<ManaStone> itemStones = item.getItemStones();

            for (ManaStone itemStone : itemStones) {
                if (count == 6) {
                    break;
                }

                StatFunction modifier = itemStone.getFirstModifier();
                if (modifier != null) {
                    count++;
                    writeH(buf, modifier.getName().getItemStoneMask());
                }
            }
            skip(buf, (6 - count) * 2);
            count = 0;
            for (ManaStone itemStone : itemStones) {
                if (count == 6) {
                    break;
                }

                StatFunction modifier = itemStone.getFirstModifier();
                if (modifier != null) {
                    count++;
                    writeH(buf, modifier.getValue());
                }
            }
            skip(buf, (6 - count) * 2);
        } else {
            skip(buf, 24);
        }
    }
}
