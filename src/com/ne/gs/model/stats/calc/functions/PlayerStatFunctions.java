/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.calc.functions;

import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;

/**
 * @author ATracer
 */
public final class PlayerStatFunctions {

    private static final List<IStatFunction> FUNCTIONS = new ArrayList<>();

    static {
        FUNCTIONS.add(new PhysicalAttackFunction());
        FUNCTIONS.add(new MagicalAttackFunction());
        FUNCTIONS.add(new AttackSpeedFunction());
        FUNCTIONS.add(new BoostCastingTimeFunction());
        FUNCTIONS.add(new PvPAttackRatioFunction());
        FUNCTIONS.add(new PDefFunction());
        FUNCTIONS.add(new MaxHpFunction());
        FUNCTIONS.add(new MaxMpFunction());

        FUNCTIONS.add(new AgilityModifierFunction(StatEnum.BLOCK, 0.25f));
        FUNCTIONS.add(new AgilityModifierFunction(StatEnum.PARRY, 0.25f));
        FUNCTIONS.add(new AgilityModifierFunction(StatEnum.EVASION, 0.3f));
    }

    public static List<IStatFunction> getFunctions() {
        return FUNCTIONS;
    }

    public static void addPredefinedStatFunctions(Player player) {
        player.getGameStats().addEffectOnly(null, FUNCTIONS);
    }
}
