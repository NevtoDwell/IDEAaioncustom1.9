/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author ATracer, xTz
 */
public class SM_SUMMON_PANEL extends AionServerPacket {

    private final Summon summon;

    public SM_SUMMON_PANEL(Summon summon) {
        this.summon = summon;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(summon.getObjectId());
        writeH(summon.getLevel());
        writeD(0);// unk
        writeD(0);// unk
        writeD(summon.getLifeStats().getCurrentHp());
        writeD(summon.getGameStats().getMaxHp().getCurrent());
        writeD(summon.getGameStats().getMainHandPAttack().getCurrent());
        writeH(summon.getGameStats().getPDef().getCurrent());
        writeH(0);
        writeH(summon.getGameStats().getMResist().getCurrent());
        writeH(0);// unk
        writeH(0);// unk
        writeD(summon.getLiveTime());
    }

}
