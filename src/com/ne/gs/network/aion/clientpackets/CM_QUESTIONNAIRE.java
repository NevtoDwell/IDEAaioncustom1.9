/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.modules.common.PollRegistry;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.HTMLService;
import com.ne.gs.services.custom.CustomQuestsService;

/**
 * @author xTz
 */
public class CM_QUESTIONNAIRE extends AionClientPacket {

    private int objectId;
    @SuppressWarnings("unused")
    private String stringItemsId;
    private List<Integer> items;

    /*
     * (non-Javadoc)
     * @see com.ne.commons.network.packet.BaseClientPacket#readImpl()
     */
    @Override
    protected void readImpl() {
        objectId = readD();
        int itemSize = readH();
        items = itemSize > 0 ? new ArrayList<Integer>(itemSize) : Collections.<Integer>emptyList();
        for (int i = 0; i < itemSize; i++) {
            items.add(readD());
        }
        stringItemsId = readS();
    }

    /*
     * (non-Javadoc)
     * @see com.ne.commons.network.packet.BaseClientPacket#runImpl()
     */
    @Override
    protected void runImpl() {
        if (objectId > 0) {
            Player player = getConnection().getActivePlayer();
            HTMLService.getReward(player, objectId, items);
            PollRegistry.query(objectId, items);
            CustomQuestsService.getInstance().removeCached(player, objectId);
        }
    }
}
