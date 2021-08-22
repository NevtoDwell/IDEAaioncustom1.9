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

import com.ne.gs.model.Race;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortalUse")
public class PortalUse {

    @XmlElement(name = "portal_path")
    protected List<PortalPath> portalPath;

    @XmlAttribute(name = "npc_id")
    protected int npcId;

    @XmlAttribute(name = "siege_id")
    protected int siegeId;

    public List<PortalPath> getPortalPaths() {
        return portalPath;
    }

    public PortalPath getPortalPath(Race race) {
        if (portalPath != null) {
            for (PortalPath path : portalPath) {
                if (path.getRace().equals(race) || path.getRace().equals(Race.PC_ALL)) {
                    return path;
                }
            }
        }
        return null;
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
