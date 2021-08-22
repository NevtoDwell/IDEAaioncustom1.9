/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team.legion;

/**
 * @author Simple
 */
public enum LegionRank {
    /**
     * All Legion Ranks *
     */
    BRIGADE_GENERAL(0),
    DEPUTY(1),
    CENTURION(2),
    LEGIONARY(3),
    VOLUNTEER(4);

    private final byte rank;

    private LegionRank(int rank) {
        this.rank = (byte) rank;
    }

    /**
     * Returns client-side id for this
     *
     * @return byte
     */
    public byte getRankId() {
        return rank;
    }
}
