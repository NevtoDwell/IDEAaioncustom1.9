/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.staticdoor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "World")
public class StaticDoorWorld {


    @XmlAttribute(name = "world")
    protected int world;
    @XmlElement(name = "staticdoor")
    protected List<StaticDoorTemplate> staticDoorTemplate;

    /**
     * @return the world
     */
    public int getWorld() {
        return world;
    }

    /**
     * @return the List<StaticDoorTemplate>
     */
    public List<StaticDoorTemplate> getStaticDoors() {
        return staticDoorTemplate;
    }
}
