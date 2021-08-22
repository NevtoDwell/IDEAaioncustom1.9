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

class MaxHpFunction extends StatFunction {

    MaxHpFunction() {
        stat = StatEnum.MAXHP;
    }

    @Override
    public void apply(Stat2 stat) {
        float health = stat.getOwner().getGameStats().getHealth().getCurrent();
        stat.setBase(Math.round(stat.getBase() * health / 100f));
    }

    @Override
    public int getPriority() {
        return 30;
    }
}
