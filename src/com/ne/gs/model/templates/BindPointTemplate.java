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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author avol
 */
@XmlRootElement(name = "bind_points")
@XmlAccessorType(XmlAccessType.NONE)
public class BindPointTemplate {

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "npcid")
    private int npcId;

    @XmlAttribute(name = "price")
    private int price;

    public String getName() {
        return name;
    }

    public int getNpcId() {
        return npcId;
    }

    public int getPrice() {
        return price;
    }
}
