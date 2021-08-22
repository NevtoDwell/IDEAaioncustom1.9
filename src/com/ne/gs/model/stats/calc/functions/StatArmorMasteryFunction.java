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
import com.ne.gs.model.templates.item.ArmorType;

/**
 * @author ATracer (based on Mr.Poke ArmorMasteryModifier)
 */
public class StatArmorMasteryFunction extends StatRateFunction {

    private final ArmorType armorType;

    public StatArmorMasteryFunction(ArmorType armorType, StatEnum name, int value, boolean bonus) {
        super(name, value, bonus);
        this.armorType = armorType;
    }

    @Override
    public void apply(Stat2 stat) {
        Player player = (Player) stat.getOwner();
        if (player.getEquipment().isArmorEquipped(armorType)) {
            super.apply(stat);
        }
    }
}
