/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.spawns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.TribeClass;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpawnModel")
public class SpawnModel {

    @XmlAttribute(name = "tribe")
    private TribeClass tribe;

    @XmlAttribute(name = "ai")
    private String ai;

    public TribeClass getTribe() {
        return tribe;
    }

    public String getAi() {
        return ai;
    }
}
