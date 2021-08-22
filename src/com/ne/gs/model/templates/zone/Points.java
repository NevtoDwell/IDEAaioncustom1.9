/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.zone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Points")
public class Points {

    @XmlElement(required = true)
    protected List<Point2D> point;
    @XmlAttribute(name = "top")
    protected float top;
    @XmlAttribute(name = "bottom")
    protected float bottom;

    public Points(){

    }
    public Points(float bottom, float top) {
        this.bottom = bottom;
        this.top = top;
    }

    /**
     * @return
     */
    public List<Point2D> getPoint() {
        if (point == null) {
            point = new ArrayList<>();
        }
        return this.point;
    }

    /**
     * @return the top
     */
    public float getTop() {
        return top;
    }

    /**
     * @return the bottom
     */
    public float getBottom() {
        return bottom;
    }

}
