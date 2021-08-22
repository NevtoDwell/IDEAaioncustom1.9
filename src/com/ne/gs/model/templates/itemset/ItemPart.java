/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.itemset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ATracer
 */
@XmlRootElement(name = "ItemPart")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemPart {

    @XmlAttribute
    protected int itemid;

    /**
     * @return the itemid
     */
    public int getItemid() {
        return itemid;
    }
}
