/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.model.summons.UnsummonType;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.summons.SummonsService;

/**
 * @author ATracer
 */
public class CM_SUMMON_COMMAND extends AionClientPacket {

    private int mode;
    private int targetObjId;

    @Override
    protected void readImpl() {
        mode = readC();
        readD();
        readD();
        targetObjId = readD();
    }

    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();
        Summon summon = activePlayer.getSummon();
        SummonMode summonMode = SummonMode.getSummonModeById(mode);
        if (summon != null && summonMode != null) {
            SummonsService.doMode(summonMode, summon, targetObjId, UnsummonType.COMMAND);
        }
    }

}
