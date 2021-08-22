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
public class StatSetFunction extends StatFunction {

    public StatSetFunction() {
    }

    public StatSetFunction(StatEnum name, int value, boolean bonus) {
        super(name, value, bonus);
    }

    @Override
    public void apply(Stat2 stat) {
        if (isBonus()) {
            stat.setBonus(getValue());
        } else {
            stat.setBase(getValue());
        }
    }

    @Override
    public final int getPriority() {
        return 10;
    }

    @Override
    public String toString() {
        return "StatSetFunction [" + super.toString() + "]";
    }

}
