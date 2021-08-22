/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.main;

import com.ne.commons.configuration.Property;

/**
 * @author Sarynth
 */
public final class RankingConfig {

    @Property(key = "gameserver.topranking.updaterule", defaultValue = "0 0 0 * * ?")
    public static String TOP_RANKING_UPDATE_RULE;
}
