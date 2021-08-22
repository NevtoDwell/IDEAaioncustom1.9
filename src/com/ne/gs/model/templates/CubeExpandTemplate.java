/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import com.ne.gs.model.templates.expand.Expand;

/**
 * @author Simple
 */
@XmlRootElement(name = "cube_npc")
@XmlAccessorType(XmlAccessType.FIELD)
public class CubeExpandTemplate {

    @XmlElement(name = "expand", required = true)
    protected List<Expand> cubeExpands;

    @XmlAttribute(name = "id", required = true)
    private int Id;

    public int getNpcId() {
        return Id;
    }

    public boolean contains(int level) {
        for (Expand expand : cubeExpands) {
            if (expand.getLevel() == level) {
                return true;
            }
        }
        return false;
    }

    public Expand get(int level) {
        for (Expand expand : cubeExpands) {
            if (expand.getLevel() == level) {
                return expand;
            }
        }
        return null;
    }
}
