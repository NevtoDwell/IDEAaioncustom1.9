/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

import com.ne.gs.model.DescId;
import com.ne.gs.model.Race;

/**
 * @author Sarynth
 */
public enum SiegeRace {

    ELYOS(0, 1800481),
    ASMODIANS(1, 1800483),
    BALAUR(2, 1800485);

    private final int _raceId;
    private final DescId _descId;

    private SiegeRace(int id, int descriptionId) {
        _raceId = id;
        _descId = DescId.of(descriptionId);
    }

    public int getRaceId() {
        return _raceId;
    }

    public static SiegeRace getByRace(Race race) {
        switch (race) {
            case ASMODIANS:
                return SiegeRace.ASMODIANS;
            case ELYOS:
                return SiegeRace.ELYOS;
            default:
                return SiegeRace.BALAUR;
        }
    }

    public DescId getDescId() {
        return _descId;
    }
}
