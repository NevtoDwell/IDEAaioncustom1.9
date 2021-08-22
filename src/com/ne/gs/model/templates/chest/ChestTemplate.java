/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.chest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Chest")
public class ChestTemplate {

    @XmlAttribute(name = "npcid")
    protected int npcId;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlElement(name = "keyitem")
    protected List<KeyItem> keyItem;

    /**
     * @return the npcId
     */
    public int getNpcId() {
        return npcId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the keyItem
     */
    public List<KeyItem> getKeyItem() {
        return keyItem;
    }
}
