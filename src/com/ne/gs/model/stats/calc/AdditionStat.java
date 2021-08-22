/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.calc;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.stats.container.StatEnum;

/**
 * @author ATracer
 */
public class AdditionStat extends Stat2 {

    public AdditionStat(StatEnum stat, int base, Creature owner) {
        super(stat, base, owner);
    }

    public AdditionStat(StatEnum stat, int base, Creature owner, float bonusRate) {
        super(stat, base, owner, bonusRate);
    }

    @Override
    public final void addToBase(int base) {
        this.base += base;
    }

    @Override
    public final void addToBonus(int bonus) {
        this.bonus += bonusRate * bonus;
    }

    @Override
    public float calculatePercent(int delta) {
        return (100 + delta) / 100f;
    }

}
