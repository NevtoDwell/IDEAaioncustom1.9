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
import java.util.List;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.model.templates.item.WeaponType;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.Skill.SkillMethod;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeaponCondition")
public class WeaponCondition extends Condition {

    @XmlAttribute(name = "weapon")
    private List<WeaponType> weaponType;

    @Override
    public boolean validate(Skill env) {
        if (env.getSkillMethod() != SkillMethod.CAST) {
            return true;
        }

        return isValidWeapon(env.getEffector());
    }

    @Override
    public boolean validate(Stat2 stat, IStatFunction statFunction) {
        return isValidWeapon(stat.getOwner());
    }

    /**
     * @param creature
     *
     * @return
     */
    private boolean isValidWeapon(Creature creature) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return weaponType.contains(player.getEquipment().getMainHandWeaponType());
        }
        // for npcs we don't validate weapon, though in templates they are present
        return true;
    }

}
