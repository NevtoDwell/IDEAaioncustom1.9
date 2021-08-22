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

class MaxMpFunction extends StatFunction {

    MaxMpFunction() {
        stat = StatEnum.MAXMP;
    }

    @Override
    public void apply(Stat2 stat) {
        float will = stat.getOwner().getGameStats().getWill().getCurrent();
        stat.setBase(Math.round(stat.getBase() * will / 100f));
    }

    @Override
    public int getPriority() {
        return 30;
    }
}
