/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CollectItem")
public class CollectItem {

    @XmlAttribute(name = "item_id")
    protected int itemId;
    @XmlAttribute
    protected int count;

    /**
     * Gets the value of the itemId property.
     *
     * @return possible object is {@link Integer }
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the value of the count property.
     *
     * @return possible object is {@link Integer }
     */
    public int getCount() {
        return count;
    }

}
