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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author orz
 */
@XmlRootElement(name = "teleporter_template")
@XmlAccessorType(XmlAccessType.NONE)
public class TeleporterTemplate {

    @XmlAttribute(name = "npc_ids")
    private List<Integer> npcIds;

    @XmlAttribute(name = "teleportId", required = true)
    private int teleportId;

    @XmlElement(name = "locations")
    private TeleLocIdData teleLocIdData;

    /**
     * @return the npcId
     */
    public List<Integer> getNpcIds() {
        return npcIds;
    }

    /**
     * @return the name of npc
     */
    public boolean containNpc(int npcId) {
        return npcIds.contains(npcId);
    }

    /**
     * @return the teleportId
     */
    public int getTeleportId() {
        return teleportId;
    }

    /**
     * @return the teleLocIdData
     */
    public TeleLocIdData getTeleLocIdData() {
        return teleLocIdData;
    }
}
