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
import com.ne.gs.model.stats.calc.StatOwner;
import com.ne.gs.model.stats.container.StatEnum;

/**
 * @author ATracer
 */
public interface IStatFunction extends Comparable<IStatFunction> {

    StatEnum getName();

    boolean isBonus();

    int getPriority();

    int getValue();

    boolean validate(Stat2 stat, IStatFunction statFunction);

    void apply(Stat2 stat);

    StatOwner getOwner();
}
