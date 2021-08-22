/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.siege.Influence;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.services.SiegeService;

/**
 * @author Nemiroff
 */
public class SM_INFLUENCE_RATIO extends AionServerPacket {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        Influence inf = Influence.getInstance();

        writeD(SiegeService.getInstance().getSecondsBeforeHourEnd());
        writeF(inf.getGlobalElyosInfluence());
        writeF(inf.getGlobalAsmodiansInfluence());
        writeF(inf.getGlobalBalaursInfluence());
        writeH(4);
        writeD(210050000);
        writeF(inf.getInggisonElyosInfluence());
        writeF(inf.getInggisonAsmodiansInfluence());
        writeF(inf.getInggisonBalaursInfluence());
        writeD(220070000);
        writeF(inf.getGelkmarosElyosInfluence());
        writeF(inf.getGelkmarosAsmodiansInfluence());
        writeF(inf.getGelkmarosBalaursInfluence());
        writeD(400010000);
        writeF(inf.getAbyssElyosInfluence());
        writeF(inf.getAbyssAsmodiansInfluence());
        writeF(inf.getAbyssBalaursInfluence());
        writeD(600030000);
        writeF(inf.getTiamarantaElyosInfluence());
        writeF(inf.getTiamarantaAsmodiansInfluence());
        writeF(inf.getTiamarantaBalaursInfluence());
    }

}
