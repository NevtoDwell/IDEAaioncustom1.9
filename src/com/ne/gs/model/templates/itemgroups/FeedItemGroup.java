/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.itemgroups;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeedItemGroup")
public abstract class FeedItemGroup {

    @XmlAttribute(name = "group", required = true)
    protected ItemGroupIndex index = ItemGroupIndex.NONE;

    @XmlElement(name = "item")
    private List<ItemRaceEntry> items;

    public ItemGroupIndex getIndex() {
        return index;
    }

    public List<ItemRaceEntry> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }
}
