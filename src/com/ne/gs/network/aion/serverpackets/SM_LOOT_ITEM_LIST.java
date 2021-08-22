/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.utils.collections.Partitioner;
import com.ne.gs.configs.main.DropConfig;
import com.ne.gs.model.drop.Drop;
import com.ne.gs.model.drop.DropItem;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemCategory;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author hex1r0
 */
public class SM_LOOT_ITEM_LIST extends AionServerPacket {

    private final int _targetUid;
    private final Collection<DropItem> _dropItems;

    @Deprecated
    private SM_LOOT_ITEM_LIST(int targetUid, Collection<DropItem> dropItems, Player player) {
        _targetUid = targetUid;
        _dropItems = new ArrayList<>(dropItems.size());

        for (DropItem item : dropItems) {
            if (item.getPlayerObjId() == 0 || player.getObjectId() == item.getPlayerObjId()) {
                _dropItems.add(item);
            }
        }
    }

    public SM_LOOT_ITEM_LIST(int targetUid, Collection<DropItem> dropItems) {
        _targetUid = targetUid;
        _dropItems = dropItems;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(_targetUid);
        writeC(_dropItems.size());

        for (DropItem dropItem : _dropItems) {
            Drop drop = dropItem.getDropTemplate();
            writeC(dropItem.getIndex()); // index in droplist
            writeD(dropItem.getDropTemplate().getItemId());
            writeD((int) dropItem.getCount());
            writeH(0);
//            writeC(0);
//            writeC(0); // 3.5
            ItemTemplate template = drop.getItemTemplate();
            writeC(!template.getCategory().equals(ItemCategory.QUEST) && !template.isTradeable() ? 1 : 0);
//            writeC(dropItem.getDropTemplate().getItemTemplate().isTradeable() ? 0 : 1);
        }
    }

    public static void sendTo(@NotNull final Player player, final int targetUid, @NotNull Set<DropItem> dropItems) {
        // rebuild indexes (temp solution)
        int i = 0;
//        for (DropItem dropItem : dropItems) {
//            dropItem.setIndex(i++);
//        }
        Partitioner<DropItem> partitioner = Partitioner.of(dropItems, DropConfig.MAX_LOOTLIST_SIZE);
        partitioner.foreach(new Partitioner.Func<DropItem>() {
            @Override
            public boolean apply(List<DropItem> dropItems1) {
                player.sendPck(new SM_LOOT_ITEM_LIST(targetUid, dropItems1, player));
                return false;
            }
        });
    }
}
