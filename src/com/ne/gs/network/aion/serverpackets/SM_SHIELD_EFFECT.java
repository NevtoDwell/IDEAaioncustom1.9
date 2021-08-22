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

import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.services.SiegeService;

/**
 * @author xTz, Source
 */
public class SM_SHIELD_EFFECT extends AionServerPacket {

    private final Collection<SiegeLocation> locations;

    public SM_SHIELD_EFFECT(Collection<SiegeLocation> locations) {
        this.locations = locations;
    }

    public SM_SHIELD_EFFECT(int fortress) {
        locations = new ArrayList<>();
        locations.add(SiegeService.getInstance().getSiegeLocation(fortress));
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeH(locations.size());
        for (SiegeLocation loc : locations) {
            writeD(loc.getLocationId());
            writeC(loc.isUnderShield() ? 1 : 0);
        }
    }

}
