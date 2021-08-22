/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;
import javolution.util.FastList;

import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.model.siege.SiegeType;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

public class SM_ABYSS_ARTIFACT_INFO extends AionServerPacket {

    private final Collection<SiegeLocation> locations;

    public SM_ABYSS_ARTIFACT_INFO(Collection<SiegeLocation> collection) {
        locations = collection;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        FastList<SiegeLocation> validLocations = new FastList<>();
        for (SiegeLocation loc : locations) {
            if (((loc.getType() == SiegeType.ARTIFACT) || (loc.getType() == SiegeType.FORTRESS)) && (loc.getLocationId() >= 1011)
                && (loc.getLocationId() < 2000)) {
                validLocations.add(loc);
            }
        }
        writeH(validLocations.size());
        for (SiegeLocation loc : validLocations) {
            writeD(loc.getLocationId());
            writeD(0); // unk
            writeD(0); // unk
        }
    }
}
