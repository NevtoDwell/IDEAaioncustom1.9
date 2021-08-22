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
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_SUMMON_UPDATE extends AionServerPacket {

    private final Summon summon;

    public SM_SUMMON_UPDATE(Summon summon) {
        this.summon = summon;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(summon.getLevel());
        writeH(summon.getMode().getId());
        writeD(0);// unk
        writeD(0);// unk
        writeD(summon.getLifeStats().getCurrentHp());

        Stat2 maxHp = summon.getGameStats().getMaxHp();
        writeD(maxHp.getCurrent());

        Stat2 mainHandPAttack = summon.getGameStats().getMainHandPAttack();
        writeD(mainHandPAttack.getCurrent());

        Stat2 pDef = summon.getGameStats().getPDef();
        writeD(pDef.getCurrent());

        Stat2 mResist = summon.getGameStats().getMResist();
        writeH(mResist.getCurrent());

        Stat2 mDef = summon.getGameStats().getMDef();
        writeD(mDef.getCurrent());
        Stat2 accuracy = summon.getGameStats().getMainHandPAccuracy();
        writeH(accuracy.getCurrent());

        Stat2 mainHandPCritical = summon.getGameStats().getMainHandPCritical();
        writeH(mainHandPCritical.getCurrent());

        Stat2 mBoost = summon.getGameStats().getMBoost();
        writeH(mBoost.getCurrent());
        Stat2 suppression = summon.getGameStats().getMBResist();
        writeH(suppression.getCurrent());

        Stat2 mAccuracy = summon.getGameStats().getMAccuracy();
        writeH(mAccuracy.getCurrent());

        Stat2 mCritical = summon.getGameStats().getMCritical();
        writeH(mCritical.getCurrent());

        Stat2 parry = summon.getGameStats().getParry();
        writeH(parry.getCurrent());

        Stat2 evasion = summon.getGameStats().getEvasion();
        writeH(evasion.getCurrent());

        writeD(maxHp.getBase());
        writeD(mainHandPAttack.getBase());
        writeD(pDef.getBase());
        writeH(mResist.getBase());
        writeD(mDef.getBase());
        writeH(accuracy.getBase());
        writeH(mainHandPCritical.getBase());
        writeH(mBoost.getBase());
        writeH(suppression.getBase());
        writeH(mAccuracy.getBase());
        writeH(mCritical.getBase());
        writeH(parry.getBase());
        writeH(evasion.getBase());
    }

}
