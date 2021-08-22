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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcSpawn")
public class NpcSpawn {

    @XmlAttribute(name = "id", required = true)
    protected Integer _id;

    @XmlAttribute(name = "respawn")
    private int _respawn;

    @XmlElement(name = "positions", required = true)
    protected PosList _positions;

    @XmlElement(name = "time")
    protected Time _time;

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        _id = id;
    }

    public int getRespawn() {
        return _respawn;
    }

    public void setRespawn(int respawn) {
        _respawn = respawn;
    }

    public PosList getPositions() {
        return _positions;
    }

    public void setPositions(PosList positions) {
        _positions = positions;
    }

    public Time getTime() {
        return _time;
    }

    public void setTime(Time value) {
        _time = value;
    }

}
