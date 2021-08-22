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
 * @author zdead
 */
public class CM_BREAK_WEAPONS extends AionClientPacket {

    private int weaponToBreakUniqueId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        readD();
        weaponToBreakUniqueId = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        ArmsfusionService.breakWeapons(getConnection().getActivePlayer(), weaponToBreakUniqueId);
    }
}
