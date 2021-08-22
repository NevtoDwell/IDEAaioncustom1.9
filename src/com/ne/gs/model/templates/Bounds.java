/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bounds")
public class Bounds extends BoundRadius {

    @XmlAttribute
    private Float altitude;

    public Bounds() {
    }

    public Bounds(float front, float side, float upper, float altitude) {
        super(front, side, upper);
        this.altitude = altitude;
    }

    public Float getAltitude() {
        return altitude;
    }
}
