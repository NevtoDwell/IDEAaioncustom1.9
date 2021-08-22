/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.ArmsfusionService;

/**
 * @author zdead modified by Wakizashi
 */
public class CM_FUSION_WEAPONS extends AionClientPacket {

    private int firstItemId;
    private int secondItemId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        readD();
        firstItemId = readD();
        secondItemId = readD();
        /*
		 * Temporary: fusion price fixed to 50000 kinah TODO: find price value
		 */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        ArmsfusionService.fusionWeapons(getConnection().getActivePlayer(), firstItemId, secondItemId);
    }
}
