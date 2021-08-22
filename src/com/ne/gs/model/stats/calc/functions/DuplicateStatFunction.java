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

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.model.templates.item.ItemTemplate;

import static ch.lambdaj.Lambda.*;

class DuplicateStatFunction extends StatFunction {

    @Override
    public void apply(Stat2 stat) {
        Item mainWeapon = ((Player) stat.getOwner()).getEquipment().getMainHandWeapon();
        Item offWeapon = ((Player) stat.getOwner()).getEquipment().getOffHandWeapon();
        if (mainWeapon != null) {
            StatFunction func1 = null;
            StatFunction func2 = null;
            List<StatFunction> functions = new ArrayList<>();
            List<StatFunction> functions1 = mainWeapon.getItemTemplate().getModifiers();
            if (functions1 != null) {
                List<StatFunction> f1 = getFunctions(functions1, stat, mainWeapon);
                if (!f1.isEmpty()) {
                    func1 = f1.get(0);
                    functions.addAll(f1);
                }
            }

            if (mainWeapon.hasFusionedItem()) {
                ItemTemplate template = mainWeapon.getFusionedItemTemplate();
                List<StatFunction> functions2 = template.getModifiers();
                if (functions2 != null) {
                    List<StatFunction> f2 = getFunctions(functions2, stat, mainWeapon);
                    if (!f2.isEmpty()) {
                        func2 = f2.get(0);
                        functions.addAll(f2);
                    }
                }
            } else if (offWeapon != null) {
                List<StatFunction> functions2 = offWeapon.getItemTemplate().getModifiers();
                if (functions2 != null) {
                    functions.addAll(getFunctions(functions2, stat, offWeapon));
                }
            }
            if ((func1 != null) && (func2 != null)) {
                if (Math.abs(func1.getValue()) >= Math.abs(func2.getValue())) {
                    functions.remove(func2);
                } else {
                    functions.remove(func1);
                }
            }
            if (!functions.isEmpty()) {
                if (getName() == StatEnum.PVP_ATTACK_RATIO) {
                    forEach(functions).apply(stat);
                } else {
                    ((StatFunction) selectMax(functions, (on(StatFunction.class)).getValue())).apply(stat);
                }
                functions.clear();
            }
            // pvp attack ratio should be stacked on dual wield
        }
    }

    private List<StatFunction> getFunctions(List<StatFunction> list, Stat2 stat, Item item) {
        List<StatFunction> functions = new ArrayList<>();
        for (StatFunction func : list) {
            StatFunctionProxy func2 = new StatFunctionProxy(item, func);
            if (func.getName() == getName() && func2.validate(stat, func2)) {
                functions.add(func);
            }
        }
        return functions;
    }

    @Override
    public int getPriority() {
        return 60;
    }

}
