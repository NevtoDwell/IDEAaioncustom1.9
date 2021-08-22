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
import com.ne.gs.model.gameobjects.player.title.Title;
import com.ne.gs.model.gameobjects.player.title.TitleList;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author cura, xTz
 */
public class SM_TITLE_INFO extends AionServerPacket {

    private TitleList titleList;
    private final int action; // 0: list, 1: self set, 3: broad set
    private int titleId;
    private int playerObjId;

    /**
     * title list
     *
     * @param player
     */
    public SM_TITLE_INFO(Player player) {
        action = 0;
        titleList = player.getTitleList();
    }

    /**
     * self title set
     *
     * @param titleId
     */
    public SM_TITLE_INFO(int titleId) {
        action = 1;
        this.titleId = titleId;
    }

    /**
     * broad title set
     *
     * @param player
     * @param titleId
     */
    public SM_TITLE_INFO(Player player, int titleId) {
        action = 3;
        playerObjId = player.getObjectId();
        this.titleId = titleId;
    }

    public SM_TITLE_INFO(boolean flag) {
        action = 4;
        titleId = flag ? 1 : 0;
    }

    public SM_TITLE_INFO(Player player, boolean flag) {
        action = 5;
        playerObjId = player.getObjectId();
        titleId = flag ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeC(action);
        switch (action) {
            case 0:
                writeC(0x00);
                writeH(titleList.size());
                for (Title title : titleList.getTitles()) {
                    writeD(title.getId());
                    writeD(title.getRemainingTime());
                }
                break;
            case 1: // self set
                writeH(titleId);
                break;
            case 3: // broad set
                writeD(playerObjId);
                writeH(titleId);
                break;
            case 4: // Mentor flag self
                writeH(titleId);
                break;
            case 5: // broad set mentor fleg
                writeD(playerObjId);
                writeH(titleId);
                break;
        }
    }
}
