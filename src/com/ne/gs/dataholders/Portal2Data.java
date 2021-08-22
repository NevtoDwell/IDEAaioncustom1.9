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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.Race;
import com.ne.gs.model.templates.portal.PortalDialog;
import com.ne.gs.model.templates.portal.PortalPath;
import com.ne.gs.model.templates.portal.PortalScroll;
import com.ne.gs.model.templates.portal.PortalUse;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"portalUse", "portalDialog", "portalScroll"})
@XmlRootElement(name = "portal_templates2")
public class Portal2Data {

    @XmlElement(name = "portal_use")
    protected List<PortalUse> portalUse;

    @XmlElement(name = "portal_dialog")
    protected List<PortalDialog> portalDialog;

    @XmlElement(name = "portal_scroll")
    protected List<PortalScroll> portalScroll;

    @XmlTransient
    private final TIntObjectHashMap<PortalUse> portalUses = new TIntObjectHashMap<>();

    @XmlTransient
    private final TIntObjectHashMap<PortalDialog> portalDialogs = new TIntObjectHashMap<>();

    @XmlTransient
    private final Map<String, PortalScroll> portalScrolls = new HashMap<>();

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (portalUse != null) {
            for (PortalUse portal : portalUse) {
                portalUses.put(portal.getNpcId(), portal);
            }
        }
        if (portalDialog != null) {
            for (PortalDialog portal : portalDialog) {
                portalDialogs.put(portal.getNpcId(), portal);
            }
        }
        if (portalScroll != null) {
            for (PortalScroll portal : portalScroll) {
                portalScrolls.put(portal.getName(), portal);
            }
        }

        portalUse = null;
        portalDialog = null;
        portalScroll = null;
    }

    public int size() {
        return portalScrolls.size() + portalDialogs.size() + portalUses.size();
    }

    public PortalPath getPortalDialog(int npcId, int dialogId, Race race) {
        PortalDialog portal = portalDialogs.get(npcId);
        if (portal != null) {
            for (PortalPath path : portal.getPortalPath()) {
                if (path.getDialog() == dialogId && (race.equals(path.getRace()) || path.getRace().equals(Race.PC_ALL))) {
                    return path;
                }
            }
        }
        return null;
    }

    public boolean isPortalNpc(int npcId) {
        return portalUses.get(npcId) != null || portalDialogs.get(npcId) != null;
    }

    public PortalUse getPortalUse(int npcId) {
        return portalUses.get(npcId);
    }

    public PortalScroll getPortalScroll(String name) {
        return portalScrolls.get(name);
    }
}
