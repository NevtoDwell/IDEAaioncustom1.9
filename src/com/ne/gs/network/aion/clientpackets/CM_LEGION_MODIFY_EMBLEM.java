/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team.legion.LegionEmblemType;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.LegionService;

/**
 * @author Simple modified cura
 */
public class CM_LEGION_MODIFY_EMBLEM extends AionClientPacket {

    /**
     * Emblem related information *
     */
    private int legionId;
    private int emblemId;
    private int red;
    private int green;
    private int blue;
    private LegionEmblemType emblemType;

    @Override
    protected void readImpl() {
        legionId = readD();
        emblemId = readC();
        emblemType = (readC() == LegionEmblemType.DEFAULT.getValue()) ? LegionEmblemType.DEFAULT : LegionEmblemType.CUSTOM;
        readC(); // 0xFF (Fixed)
        red = readC();
        green = readC();
        blue = readC();
    }

    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();

        if (activePlayer.isLegionMember()) {
            LegionService.getInstance().storeLegionEmblem(activePlayer, legionId, emblemId, red, green, blue, emblemType);
        }
    }
}
