/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.change;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.skillengine.condition.Conditions;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Change")
public class Change {

    @XmlAttribute(required = true)
    private StatEnum stat;
    @XmlAttribute(required = true)
    private Func func;
    @XmlAttribute(required = true)
    private int value;
    @XmlAttribute
    private int delta;
    @XmlElement(name = "conditions")
    private Conditions conditions;

    public final StatEnum getStat() {
        return stat;
    }

    public final Func getFunc() {
        return func;
    }

    public final int getValue() {
        return value;
    }

    public final int getDelta() {
        return delta;
    }

    public final Conditions getConditions() {
        return conditions;
    }

}
