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

class PDefFunction extends StatFunction {

    PDefFunction() {
        stat = StatEnum.PHYSICAL_DEFENSE;
    }

    @Override
    public void apply(Stat2 stat) {
        if (stat.getOwner().isInFlyingState()) {
            stat.setBonus(stat.getBonus() - (stat.getBase() / 2));
        }
    }

    @Override
    public int getPriority() {
        return 60;
    }
}
