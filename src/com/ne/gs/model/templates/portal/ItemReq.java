/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.portal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemReq")
public class ItemReq {

    @XmlAttribute(name = "item_id")
    protected int itemId;

    @XmlAttribute(name = "item_count")
    protected int itemCount;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int value) {
        itemId = value;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int value) {
        itemCount = value;
    }
}
