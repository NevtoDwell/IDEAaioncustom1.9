/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.world.World;

/**
 * @author ATracer
 */
public class SM_ITEM_USAGE_ANIMATION extends AionServerPacket {

    private final int playerObjId;
    private final int targetObjId;
    private final int itemObjId;
    private final int itemId;
    private final int time;
    private final int end;
    private int unk;

    public SM_ITEM_USAGE_ANIMATION(int playerObjId, int itemObjId, int itemId) {
        this.playerObjId = playerObjId;
        targetObjId = playerObjId;
        this.itemObjId = itemObjId;
        this.itemId = itemId;
        time = 0;
        end = 1;
        unk = 1;
    }

    public SM_ITEM_USAGE_ANIMATION(int playerObjId, int itemObjId, int itemId, int time, int end) {
        this.playerObjId = playerObjId;
        targetObjId = playerObjId;
        this.itemObjId = itemObjId;
        this.itemId = itemId;
        this.time = time;
        this.end = end;
    }

    public SM_ITEM_USAGE_ANIMATION(int playerObjId, int itemObjId, int itemId, int time, int end, int unk) {
        this.playerObjId = playerObjId;
        targetObjId = playerObjId;
        this.itemObjId = itemObjId;
        this.itemId = itemId;
        this.time = time;
        this.end = end;
        this.unk = unk;
    }

    public SM_ITEM_USAGE_ANIMATION(int playerObjId, int targetObjId, int itemObjId, int itemId, int time, int end,
                                   int unk) {
        this.playerObjId = playerObjId;
        this.targetObjId = targetObjId;
        this.itemObjId = itemObjId;
        this.itemId = itemId;
        this.time = time;
        this.end = end;
        this.unk = unk;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        if (time > 0) {
            Player player = World.getInstance().findPlayer(playerObjId);
            Item item = player.getInventory().getItemByObjId(itemObjId);
            player.setUsingItem(item);
        }

        writeD(playerObjId); // player obj id
        writeD(targetObjId); // target obj id

        writeD(itemObjId); // itemObjId
        writeD(itemId); // item id

        writeD(time); // unk
        writeC(end); // unk
        writeC(0); // unk
        writeC(1);
        writeD(unk);
        writeC(0);// unk
    }
}
