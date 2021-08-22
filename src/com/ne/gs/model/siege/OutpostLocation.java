/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

import java.util.List;

import com.ne.gs.model.templates.siegelocation.SiegeLocationTemplate;

/**
 * @author Source
 *         These bosses only appear when an faction conquer all balaurea fortress... If Elyos conquer all fortress the Enraged Mastarius appear on Ancient City
 *         of Marayas If Asmodians conquer all fortress the Enraged Veille appear on Inggison Outpost He/She still active for 2 hours after that he/she
 *         disappear and respawn again next day on the end of Siege (if the faction owns all fortress)
 */
public class OutpostLocation extends SiegeLocation {

    public OutpostLocation() {
    }

    public OutpostLocation(SiegeLocationTemplate template) {
        super(template);
    }

    @Override
    public int getNextState() {
        return isVulnerable() ? STATE_INVULNERABLE : STATE_VULNERABLE;
    }

    /**
     * @return Outpost Location Race
     *
     * @deprecated Should be configured from datapack
     */
    @Deprecated
    public SiegeRace getLocationRace() {
        switch (getLocationId()) {
            case 3111:
                return SiegeRace.ASMODIANS;
            case 2111:
                return SiegeRace.ELYOS;
            default:
                throw new RuntimeException("Please move this to datapack");
        }
    }

    /**
     * @return Fortresses that must be captured to own this outpost
     */
    public List<Integer> getFortressDependency() {
        return template.getFortressDependency();
    }

    public boolean isSiegeAllowed() {
        return getLocationRace() == getRace();
    }

    public boolean isSilentraAllowed() {
        return !isSiegeAllowed() && !getRace().equals(SiegeRace.BALAUR);
    }
}
