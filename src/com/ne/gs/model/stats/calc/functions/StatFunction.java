/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.calc.functions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.calc.StatOwner;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.skillengine.condition.Conditions;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleModifier")
public class StatFunction implements IStatFunction {

    @XmlAttribute(name = "name")
    protected StatEnum stat;
    @XmlAttribute
    private boolean bonus;
    @XmlAttribute
    protected int value;
    @XmlElement(name = "conditions")
    private Conditions conditions;

    public StatFunction() {
    }

    public StatFunction(StatEnum stat, int value, boolean bonus) {
        this.stat = stat;
        this.value = value;
        this.bonus = bonus;
    }

    @Override
    public int compareTo(IStatFunction o) {
        int result = getPriority() - o.getPriority();
        if (result == 0) {
            return hashCode() - o.hashCode();
        }
        return result;
    }

    @Override
    public StatOwner getOwner() {
        return null;
    }

    @Override
    public StatEnum getName() {
        return stat;
    }

    @Override
    public boolean isBonus() {
        return bonus;
    }

    @Override
    public int getPriority() {
        return 0x10;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public boolean validate(Stat2 stat, IStatFunction statFunction) {
        return conditions == null || conditions.validate(stat, statFunction);
    }

    @Override
    public void apply(Stat2 stat) {
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [stat=" + stat + ", bonus=" + bonus + ", value=" + value + ", priority=" + getPriority() + "]";
    }

    public StatFunction withConditions(Conditions conditions) {
        this.conditions = conditions;
        return this;
    }

}
