/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.portal.PortalLoc;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"portalLoc"})
@XmlRootElement(name = "portal_locs")
public class PortalLocData {

    @XmlElement(name = "portal_loc")
    protected List<PortalLoc> portalLoc;

    @XmlTransient
    private final TIntObjectHashMap<PortalLoc> portalLocs = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        for (PortalLoc loc : portalLoc) {
            portalLocs.put(loc.getLocId(), loc);
        }

        portalLoc = null;
    }

    public int size() {
        return portalLocs.size();
    }

    public PortalLoc getPortalLoc(int locId) {
        return portalLocs.get(locId);
    }
}
