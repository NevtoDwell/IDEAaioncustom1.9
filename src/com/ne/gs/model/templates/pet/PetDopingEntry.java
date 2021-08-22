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

@XmlType(name = "dope")
@XmlAccessorType(XmlAccessType.NONE)
public class PetDopingEntry {

    @XmlAttribute(name = "id", required = true)
    private short id;

    @XmlAttribute(name = "usedrink", required = true)
    private boolean usedrink;

    @XmlAttribute(name = "usefood", required = true)
    private boolean usefood;

    @XmlAttribute(name = "usescroll", required = true)
    private int usescroll;

    public short getId() {
        return id;
    }

    public boolean isUseDrink() {
        return usedrink;
    }

    public boolean isUseFood() {
        return usefood;
    }

    public int getScrollsUsed() {
        return usescroll;
    }
}
