/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.modules;

import com.ne.commons.configuration.Property;

/**
 * @author hex1r0
 */
public final class PvPApAccumulatorConfig {

    /**
     * Enable accumulation of pvp ap into separate table pvp_ap_accumulator
     */
    @Property(key = "modules.pvpapaccumulator.enabled", defaultValue = "false")
    public static boolean PVP_AP_ACCUMULATOR_ENABLED;
}
