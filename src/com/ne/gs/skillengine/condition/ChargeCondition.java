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

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.calc.StatOwner;
import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChargeCondition")
public class ChargeCondition extends Condition {

    @XmlAttribute(name = "level")
    private int level;

    @Override
    public boolean validate(Stat2 env, IStatFunction statFunction) {
        StatOwner owner = statFunction.getOwner();
        if (owner instanceof Item) {
            Item item = (Item) owner;
            return item.getChargeLevel() >= level;
        }
        return false;
    }

    @Override
    public boolean validate(Skill env) {
        return false;
    }
}
