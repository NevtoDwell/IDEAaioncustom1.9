/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

public class SM_SIEGE_LOCATION_STATE extends AionServerPacket {

    private final int locationId;
    private final int state;

    public SM_SIEGE_LOCATION_STATE(SiegeLocation location) {
        locationId = location.getLocationId();
        state = (location.isVulnerable() ? 1 : 0);
    }

    public SM_SIEGE_LOCATION_STATE(int locationId, int state) {
        this.locationId = locationId;
        this.state = state;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(locationId);
        writeC(state);
    }
}
