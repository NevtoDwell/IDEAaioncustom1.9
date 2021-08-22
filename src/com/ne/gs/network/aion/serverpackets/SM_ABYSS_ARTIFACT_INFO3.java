/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.Collection;

import com.ne.gs.model.siege.ArtifactLocation;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.services.SiegeService;

public class SM_ABYSS_ARTIFACT_INFO3 extends AionServerPacket {

    private final Collection<ArtifactLocation> locations;

    public SM_ABYSS_ARTIFACT_INFO3(Collection<ArtifactLocation> collection) {
        locations = collection;
    }

    public SM_ABYSS_ARTIFACT_INFO3(int loc) {
        locations = new ArrayList<>();
        locations.add(SiegeService.getInstance().getArtifact(loc));
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeH(locations.size());
        for (ArtifactLocation artifact : locations) {
            writeD(artifact.getLocationId() * 10 + 1);
            writeC(artifact.getStatus().getValue());
            writeD(0);
        }
    }
}
