/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.calc.functions;

import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.container.StatEnum;

/**
 * @author ATracer
 */
public class StatAddFunction extends StatFunction {

    public StatAddFunction() {
    }

    public StatAddFunction(StatEnum name, int value, boolean bonus) {
        super(name, value, bonus);
    }

    @Override
    public void apply(Stat2 stat) {
        if (isBonus()) {
            stat.addToBonus(getValue());
        } else {
            stat.addToBase(getValue());
        }
    }

    @Override
    public int getPriority() {
        return isBonus() ? 50 : 30;
    }

    @Override
    public String toString() {
        return "StatAddFunction [" + super.toString() + "]";
    }

}
