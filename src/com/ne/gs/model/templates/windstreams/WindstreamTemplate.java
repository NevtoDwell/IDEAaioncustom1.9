/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.windstreams;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author LokiReborn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WindFlight")
public class WindstreamTemplate {

    @XmlElement(required = true)
    protected StreamLocations locations;
    @XmlAttribute
    protected int mapid;

    /**
     * Gets the value of the locations property.
     */
    public StreamLocations getLocations() {
        return locations;
    }

    /**
     * Gets the value of the mapid property.
     */
    public int getMapid() {
        return mapid;
    }
}
