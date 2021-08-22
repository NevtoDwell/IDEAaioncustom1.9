/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import com.ne.gs.model.stats.calc.functions.StatFunction;

/**
 * @author xavier
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "modifiers")
public class ModifiersTemplate {

    @XmlElements({
        @XmlElement(name = "sub", type = com.ne.gs.model.stats.calc.functions.StatSubFunction.class),
        @XmlElement(name = "add", type = com.ne.gs.model.stats.calc.functions.StatAddFunction.class),
        @XmlElement(name = "rate", type = com.ne.gs.model.stats.calc.functions.StatRateFunction.class),
        @XmlElement(name = "set", type = com.ne.gs.model.stats.calc.functions.StatSetFunction.class)})
    private List<StatFunction> modifiers;

    @XmlAttribute
    private float chance = 100;

    public List<StatFunction> getModifiers() {
        return modifiers;
    }

    public float getChance() {
        return chance;
    }
}
