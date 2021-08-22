/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author antness
 */
@XmlType(name = "DecomposableItem")
public class DecomposableItemInfo {

    @XmlAttribute(name = "item_id")
    private int itemId;
    @XmlElement(name = "items")
    private List<ExtractedItemsCollection> itemsCollections;

    public int getItemId() {
        return itemId;
    }

    public List<ExtractedItemsCollection> getItemsCollections() {
        return itemsCollections;
    }
}
