/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.calc.functions;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.container.StatEnum;

/**
 * @author VladimirZ
 */
public class StatShieldMasteryFunction extends StatRateFunction {

    public StatShieldMasteryFunction(StatEnum name, int value, boolean bonus) {
        super(name, value, bonus);
    }

    @Override
    public void apply(Stat2 stat) {
        Player player = (Player) stat.getOwner();
        if (player.getEquipment().isShieldEquipped()) {
            super.apply(stat);
        }
    }
}
