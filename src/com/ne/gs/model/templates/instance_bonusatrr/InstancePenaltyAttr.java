/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.instance_bonusatrr;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.skillengine.change.Func;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstancePenaltyAttr")
public class InstancePenaltyAttr {

    @XmlAttribute(required = true)
    protected StatEnum stat;

    @XmlAttribute(required = true)
    protected Func func;

    @XmlAttribute(required = true)
    protected int value;

    public StatEnum getStat() {
        return stat;
    }

    public void setStat(StatEnum value) {
        stat = value;
    }

    public Func getFunc() {
        return func;
    }

    public void setFunc(Func value) {
        func = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
