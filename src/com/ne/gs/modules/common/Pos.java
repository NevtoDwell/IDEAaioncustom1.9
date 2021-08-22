/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.commons.math.AionPos;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Pos")
public class Pos implements AionPos {

    @XmlAttribute(name = "mapId", required = true)
    protected int _mapId;

    @XmlAttribute(name = "x", required = true)
    protected float _x;

    @XmlAttribute(name = "y", required = true)
    protected float _y;

    @XmlAttribute(name = "z", required = true)
    protected float _z;

    @XmlAttribute(name = "h")
    protected int _h;

    @XmlAttribute(name = "handler")
    protected String _handler;

    public int getMapId() {
        return _mapId;
    }

    public void setMapId(int value) {
        _mapId = value;
    }

    public float getX() {
        return _x;
    }

    public void setX(float value) {
        _x = value;
    }

    public float getY() {
        return _y;
    }

    public void setY(float value) {
        _y = value;
    }

    public float getZ() {
        return _z;
    }

    public void setZ(float value) {
        _z = value;
    }

    public int getH() {
        return _h;
    }

    public void setH(int h) {
        _h = h;
    }

    public String getHandler() {
        return _handler;
    }

    public void setHandler(String value) {
        _handler = value;
    }

}
