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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author ATracer
 */
@XmlRootElement(name = "locations")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeleLocIdData {

    @XmlElement(name = "telelocation")
    private List<TeleportLocation> locids;

    /**
     * @return Teleport locations
     */
    public List<TeleportLocation> getTelelocations() {
        return locids;
    }

    public TeleportLocation getTeleportLocation(int value) {
        for (TeleportLocation t : locids) {
            if (t != null && t.getLocId() == value) {
                return t;
            }
        }
        return null;
    }
}
