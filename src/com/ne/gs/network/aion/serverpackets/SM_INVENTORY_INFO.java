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
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;

/**
 * In this packet Server is sending Inventory Info
 *
 * @author -Nemesiss-
 * @updater alexa026
 * @finisher Avol ;d modified by ATracer
 * @fixedby -Nemesiss- :D
 */
public class SM_INVENTORY_INFO extends AionServerPacket {

    public static final int EMPTY = 0;
    public static final int FULL = 1;
    public int npcExpandsSize = 0;
    public int questExpandsSize = 0;

    private List<Item> items;
    private Player player;

    public int packetType = FULL;

    public SM_INVENTORY_INFO(List<Item> items, int npcExpandsSize, int questExpandsSize, Player player) {
        this.items = items;
        this.npcExpandsSize = npcExpandsSize;
        this.questExpandsSize = questExpandsSize;
        this.player = player;
    }

    public SM_INVENTORY_INFO() {
        packetType = EMPTY;
    }


    @Override
    protected void writeImpl(AionConnection con) {
        if (packetType == EMPTY) {
            writeD(0);
            writeH(0);
            return;
        }

        // something wrong with cube part.
        writeC(1); // TRUE/FALSE (1/0) update cube size
        writeC(npcExpandsSize); // cube size from npc (so max 5 for now)
        writeC(questExpandsSize); // cube size from quest (so max 2 for now)
        writeC(0); // unk?
        writeH(items.size()); // number of entries

        for (Item item : items) {
            writeItemInfo(item);
        }
    }

    private void writeItemInfo(Item item) {
        ItemTemplate itemTemplate = item.getItemTemplate();

        writeD(item.getObjectId());
        writeD(itemTemplate.getTemplateId());
        writeNameId(itemTemplate.getNameId());

        ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
        itemInfoBlob.writeMe(getBuf());

        writeH(item.isEquipped() ? 255 : item.getEquipmentSlot()); // FF FF equipment
        writeC(0x00);// isEquiped?
    }
}
