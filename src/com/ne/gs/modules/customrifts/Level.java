/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.customrifts;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Level")
public class Level {

    @XmlAttribute(name = "min")
    protected int _min;

    @XmlAttribute(name = "max")
    protected int _max;

    public int getMin() {
        return _min;
    }

    public void setMin(int min) {
        _min = min;
    }

    public int getMax() {
        return _max;
    }

    public void setMax(int max) {
        _max = max;
    }
}
