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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InventoryItems", propOrder = {"inventoryItem"})
public class InventoryItems {

    @XmlElement(name = "inventory_item")
    protected List<InventoryItem> inventoryItem;

    public List<InventoryItem> getInventoryItem() {
        if (inventoryItem == null) {
            inventoryItem = new ArrayList<>(0);
        }
        return inventoryItem;
    }
}
