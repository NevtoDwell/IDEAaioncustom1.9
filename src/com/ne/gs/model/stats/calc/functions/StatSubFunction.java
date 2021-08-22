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

/**
 * @author ATracer
 */
public class StatSubFunction extends StatFunction {

    @Override
    public void apply(Stat2 stat) {
        if (isBonus()) {
            stat.addToBonus(-getValue());
        } else {
            stat.addToBase(-getValue());
        }
    }

    @Override
    public final int getPriority() {
        return isBonus() ? 50 : 30;
    }

}
