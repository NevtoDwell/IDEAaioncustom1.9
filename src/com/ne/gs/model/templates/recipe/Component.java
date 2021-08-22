/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.recipe;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Component")
public class Component {

    @XmlAttribute
    protected int itemid;
    @XmlAttribute
    protected int quantity;

    /**
     * Gets the value of the itemid property.
     */
    public Integer getItemid() {
        return itemid;
    }

    /**
     * Gets the value of the quantity property.
     */
    public Integer getQuantity() {
        return quantity;
    }
}
