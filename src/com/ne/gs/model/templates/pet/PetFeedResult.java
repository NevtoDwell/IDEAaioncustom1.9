/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.pet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetFeedResult")
public class PetFeedResult {

    @XmlAttribute(required = true)
    protected int item;

    @XmlAttribute
    protected String name;

    public int getItem() {
        return item;
    }

    @Override
    public String toString() {
        return name + " (" + item + ")";
    }
}
