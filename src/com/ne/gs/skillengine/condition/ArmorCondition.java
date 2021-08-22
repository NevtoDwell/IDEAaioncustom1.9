/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.model.templates.item.ArmorType;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArmorCondition")
public class ArmorCondition extends Condition {

    @XmlAttribute(name = "armor")
    private ArmorType armorType;

    @Override
    public boolean validate(Skill env) {
        return isValidArmor(env.getEffector());
    }

    @Override
    public boolean validate(Stat2 stat, IStatFunction statFunction) {
        return isValidArmor(stat.getOwner());
    }

    @Override
    public boolean validate(Effect effect) {
        return isValidArmor(effect.getEffector());
    }

    /**
     * @param creature
     *
     * @return
     */
    private boolean isValidArmor(Creature creature) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.getEquipment().isArmorTypeEquipped(armorType);
        }
        return false;
    }

}
