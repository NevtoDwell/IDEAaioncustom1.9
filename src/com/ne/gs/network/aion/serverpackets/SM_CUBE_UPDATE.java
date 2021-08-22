/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_CUBE_UPDATE extends AionServerPacket {

    private final int action;
    /**
     * for action 0 - its storage type<br>
     * for action 6 - its advanced stigma count
     */
    private final int actionValue;

    private int itemsCount;
    private int npcExpands;
    private int questExpands;

    public static SM_CUBE_UPDATE stigmaSlots(int slots) {
        return new SM_CUBE_UPDATE(6, slots);
    }

    public static SM_CUBE_UPDATE cubeSize(StorageType type, Player player) {
        int itemsCount = 0;
        int npcExpands = 0;
        int questExpands = 0;
        switch (type) {
            case CUBE:
                itemsCount = player.getInventory().size();
                npcExpands = player.getNpcExpands();
                questExpands = player.getQuestExpands();
                break;
            case REGULAR_WAREHOUSE:
                itemsCount = player.getWarehouse().size();
                npcExpands = player.getWarehouseSize();
                // questExpands = ?? //TODO!
                break;
            case LEGION_WAREHOUSE:
                itemsCount = player.getLegion().getLegionWarehouse().size();
                npcExpands = player.getLegion().getWarehouseLevel();
                break;
        }

        return new SM_CUBE_UPDATE(0, type.ordinal(), itemsCount, npcExpands, questExpands);
    }

    private SM_CUBE_UPDATE(int action, int actionValue, int itemsCount, int npcExpands, int questExpands) {
        this(action, actionValue);
        this.itemsCount = itemsCount;
        this.npcExpands = npcExpands;
        this.questExpands = questExpands;
    }

    private SM_CUBE_UPDATE(int action, int actionValue) {
        this.action = action;
        this.actionValue = actionValue;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(action);
        writeC(actionValue);
        switch (action) {
            case 0:
                writeD(itemsCount);
                writeC(npcExpands); // cube size from npc (so max 5 for now)
                writeC(questExpands); // cube size from quest (so max 2 for now)
                writeC(0); // unk - expands from items?
                break;
            case 6:
                break;
            default:
                break;
        }
    }
}
