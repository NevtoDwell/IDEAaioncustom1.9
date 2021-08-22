/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.List;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.items.GodStone;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Avol modified by ATracer
 */
public class SM_UPDATE_PLAYER_APPEARANCE extends AionServerPacket {

    public int playerId;
    public int size;
    public List<Item> items;

    public SM_UPDATE_PLAYER_APPEARANCE(int playerId, List<Item> items) {
        this.playerId = playerId;
        this.items = items;
        size = items.size();
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playerId);

        short mask = 0;
        for (Item item : items) {
            mask |= item.getEquipmentSlot();
        }

        writeH(mask);

        for (Item item : items) {
            writeD(item.getItemSkinTemplate().getTemplateId());
            GodStone godStone = item.getGodStone();
            writeD(godStone != null ? godStone.getItemId() : 0);
            writeD(item.getItemColor());
            writeH(0x00);// unk (0x00)
        }
    }
}
