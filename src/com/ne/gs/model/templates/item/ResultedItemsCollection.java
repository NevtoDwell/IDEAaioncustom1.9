/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author antness
 */
@XmlType(name = "ResultedItemsCollection")
public class ResultedItemsCollection {

    @XmlElement(name = "item")
    protected ArrayList<ResultedItem> items;
    @XmlElement(name = "random_item")
    protected ArrayList<RandomItem> randomItems;

    public Collection<ResultedItem> getItems() {
        return items != null ? items : Collections.<ResultedItem>emptyList();
    }

    public List<RandomItem> getRandomItems() {
        if (randomItems != null) {
            return randomItems;
        } else {
            return new ArrayList<>();
        }
    }
}
