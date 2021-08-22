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
import javax.xml.bind.annotation.XmlType;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeyItem")
public class KeyItem {

    @XmlAttribute(name = "id")
    protected int id;
    @XmlAttribute(name = "itemid")
    protected int itemid;
    @XmlAttribute(name = "quantity")
    protected int quantity;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the itemid
     */
    public int getItemId() {
        return itemid;
    }

    /**
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }
}
