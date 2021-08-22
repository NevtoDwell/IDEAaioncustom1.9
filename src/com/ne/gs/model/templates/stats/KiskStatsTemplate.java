/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Sarynth
 */

@XmlRootElement(name = "kisk_stats")
@XmlAccessorType(XmlAccessType.FIELD)
public class KiskStatsTemplate {

    @XmlAttribute(name = "usemask")
    private int useMask = 4;

    @XmlAttribute(name = "members")
    private int maxMembers = 6;

    @XmlAttribute(name = "resurrects")
    private int maxResurrects = 18;

    public int getUseMask() {
        return useMask;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public int getMaxResurrects() {
        return maxResurrects;
    }
}
