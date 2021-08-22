/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.container;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.stats.calc.Stat2;

/**
 * @author ATracer
 */
public class TrapGameStats extends NpcGameStats {

    public TrapGameStats(Npc owner) {
        super(owner);
    }

    @Override
    public Stat2 getStat(StatEnum statEnum, int base) {
        Stat2 stat = super.getStat(statEnum, base);
        if (owner.getMaster() == null) {
            return stat;
        }
        switch (statEnum) {
            case BOOST_MAGICAL_SKILL:
            case MAGICAL_ACCURACY:
                // bonus is calculated from stat bonus of master (only green value)
                stat.setBonusRate(0.7f); // TODO: retail formula?
                return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);

        }
        return stat;
    }

}
