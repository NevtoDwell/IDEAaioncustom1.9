/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.ai;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Ai")
public class Ai {

    @XmlElement(name = "summons")
    private Summons summons;

    @XmlElement(name = "bombs")
    private Bombs bombs;

    @XmlAttribute(name = "npcId")
    private int npcId;

    public Summons getSummons() {
        return summons;
    }

    public Bombs getBombs() {
        return bombs;
    }

    public int getNpcId() {
        return npcId;
    }

}
