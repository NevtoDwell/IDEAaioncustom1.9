/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Item")
public class Item {

    @XmlAttribute(name = "itemId")
    protected int _itemId;

    @XmlAttribute(name = "count")
    protected long _count;

    public int getItemId() {
        return _itemId;
    }

    public void setItemId(int itemId) {
        _itemId = itemId;
    }

    public long getCount() {
        return _count;
    }

    public void setCount(long count) {
        _count = count;
    }
}
