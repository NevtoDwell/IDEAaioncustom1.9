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
import com.ne.gs.model.templates.item.WeaponType;

/**
 * @author ATracer (based on Mr.Poke WeaponMasteryModifier)
 */
public class StatWeaponMasteryFunction extends StatRateFunction {

    private final WeaponType weaponType;

    public StatWeaponMasteryFunction(WeaponType weaponType, StatEnum name, int value, boolean bonus) {
        super(name, value, bonus);
        this.weaponType = weaponType;
    }

    @Override
    public void apply(Stat2 stat) {
        Player player = (Player) stat.getOwner();
        switch (this.stat) {
            case MAIN_HAND_POWER:
                if (player.getEquipment().getMainHandWeaponType() == weaponType) {
                    super.apply(stat);
                }
                break;
            case OFF_HAND_POWER:
                if (player.getEquipment().getOffHandWeaponType() == weaponType) {
                    super.apply(stat);
                }
                break;
            default:
                if (player.getEquipment().getMainHandWeaponType() == weaponType) {
                    super.apply(stat);
                }
        }

    }

}
