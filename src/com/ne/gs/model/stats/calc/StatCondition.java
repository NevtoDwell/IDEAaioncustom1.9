/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.calc;

import com.ne.gs.model.stats.calc.functions.IStatFunction;

/**
 * @author ATracer
 */
public interface StatCondition {

    /**
     * Validate that function should be applied to the stat
     *
     * @param stat
     * @param statFunction
     *
     * @return
     */
    boolean validate(Stat2 stat, IStatFunction statFunction);
}
