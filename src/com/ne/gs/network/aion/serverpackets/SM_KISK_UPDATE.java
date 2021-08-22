/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Kisk;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Sarynth 0xB0 for 1.5.1.10 and 1.5.1.15
 */
public class SM_KISK_UPDATE extends AionServerPacket {

    // useMask values determine who can bind to the kisk.
    // 1 ~ race
    // 2 ~ legion
    // 3 ~ solo
    // 4 ~ group
    // 5 ~ alliance
    // of course, we must programmatically check as well.

    private final int objId;
    private final int useMask;
    private final int currentMembers;
    private final int maxMembers;
    private final int remainingRessurects;
    private final int maxRessurects;
    private final int remainingLifetime;

    public SM_KISK_UPDATE(Kisk kisk) {
        objId = kisk.getObjectId();
        useMask = kisk.getUseMask();
        currentMembers = kisk.getCurrentMemberCount();
        maxMembers = kisk.getMaxMembers();
        remainingRessurects = kisk.getRemainingResurrects();
        maxRessurects = kisk.getMaxRessurects();
        remainingLifetime = kisk.getRemainingLifetime();
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(objId);
        writeD(useMask);
        writeD(currentMembers);
        writeD(maxMembers);
        writeD(remainingRessurects);
        writeD(maxRessurects);
        writeD(remainingLifetime);
    }

}
