/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author hex1r0
 */
public final class SM_LOOT_STATE extends AionServerPacket {
    public static final int HAVE_LOOT = 0;
    public static final int LACK_LOOT = 1;
    public static final int SHOW_LIST = 2;
    public static final int HIDE_LIST = 3;

    private final int _npcUid;
    private final int _action;

    public SM_LOOT_STATE(int npcUid, int action) {
        _npcUid = npcUid;
        _action = action;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(_npcUid);
        writeC(_action);
    }

    public static SM_LOOT_STATE doHasLoot(int npcUid) {
        return new SM_LOOT_STATE(npcUid, HAVE_LOOT);
    }

    public static SM_LOOT_STATE doLacksLoot(int npcUid) {
        return new SM_LOOT_STATE(npcUid, LACK_LOOT);
    }

    public static SM_LOOT_STATE showList(int npcUid) {
        return new SM_LOOT_STATE(npcUid, SHOW_LIST);
    }

    public static SM_LOOT_STATE hideList(int npcUid) {
        return new SM_LOOT_STATE(npcUid, HIDE_LIST);
    }
}