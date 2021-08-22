/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.templates.item.AssemblyItem;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"item"})
@XmlRootElement(name = "assembly_items")
public class AssemblyItemsData {

    @XmlElement(required = true)
    protected List<AssemblyItem> item;

    @XmlTransient
    private final List<AssemblyItem> items = new ArrayList<>();

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        for (AssemblyItem template : item) {
            items.add(template);
        }

        item = null;
    }

    public int size() {
        return items.size();
    }

    public AssemblyItem getAssemblyItem(int itemId) {
        for (AssemblyItem assemblyItem : items) {
            if (assemblyItem.getId() == itemId) {
                return assemblyItem;
            }
        }
        return null;
    }
}
