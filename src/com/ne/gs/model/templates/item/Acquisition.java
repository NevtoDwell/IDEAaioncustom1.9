/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Acquisition")
public class Acquisition {

    @XmlAttribute(name = "ap", required = false)
    private int ap;

    @XmlAttribute(name = "count", required = false)
    private int itemCount;

    @XmlAttribute(name = "item", required = false)
    private int itemId;

    @XmlAttribute(name = "type", required = true)
    private AcquisitionType type;

    public AcquisitionType getType() {
        return type;
    }

    public int getItemId() {
        return itemId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getRequiredAp() {
        return ap;
    }
}
