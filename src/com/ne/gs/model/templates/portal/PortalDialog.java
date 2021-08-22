/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.portal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortalDialog", propOrder = {"portalPath"})
public class PortalDialog {

    @XmlElement(name = "portal_path")
    protected List<PortalPath> portalPath;

    @XmlAttribute(name = "npc_id")
    protected int npcId;

    @XmlAttribute(name = "siege_id")
    protected int siegeId;

    public List<PortalPath> getPortalPath() {
        return portalPath;
    }

    public int getNpcId() {
        return npcId;
    }

    public void setNpcId(int value) {
        npcId = value;
    }

    public int getSiegeId() {
        return siegeId;
    }

    public void setSiegeId(int value) {
        siegeId = value;
    }
}
