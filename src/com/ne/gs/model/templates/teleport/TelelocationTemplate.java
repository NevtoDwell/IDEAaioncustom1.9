/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.teleport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author orz
 */
@XmlRootElement(name = "teleloc_template")
@XmlAccessorType(XmlAccessType.NONE)
public class TelelocationTemplate {

    /**
     * Location Id.
     */
    @XmlAttribute(name = "loc_id", required = true)
    private int locId;

    @XmlAttribute(name = "mapid", required = true)
    private int mapid = 0;
    /**
     * location name.
     */
    @XmlAttribute(name = "name", required = true)
    private String name = "";

    @XmlAttribute(name = "name_id", required = true)
    private int nameId;

    @XmlAttribute(name = "posX")
    private float x = 0;

    @XmlAttribute(name = "posY")
    private float y = 0;

    @XmlAttribute(name = "posZ")
    private float z = 0;

    @XmlAttribute(name = "heading")
    private int heading = 0;

    public int getLocId() {
        return locId;
    }

    public int getMapId() {
        return mapid;
    }

    public String getName() {
        return name;
    }

    public int getNameId() {
        return nameId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public int getHeading() {
        return heading;
    }
}
