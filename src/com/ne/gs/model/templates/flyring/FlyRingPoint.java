/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.flyring;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.utils3d.Point3D;

/**
 * @author M@xx
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlyRingPoint")
public class FlyRingPoint {

    @XmlAttribute(name = "x")
    private float x;

    @XmlAttribute(name = "y")
    private float y;

    @XmlAttribute(name = "z")
    private float z;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public FlyRingPoint() {
    }

    public FlyRingPoint(Point3D p) {
        x = (float) p.x;
        y = (float) p.y;
        z = (float) p.z;
    }
}
