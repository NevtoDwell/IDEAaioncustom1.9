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
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssemblyItem")
public class AssemblyItem {

    @XmlAttribute(required = true)
    protected List<Integer> parts;

    @XmlAttribute(required = true)
    protected int id;

    public List<Integer> getParts() {
        if (parts == null) {
            parts = new ArrayList<>();
        }
        return parts;
    }

    public int getId() {
        return id;
    }

    public void setId(int value) {
        id = value;
    }
}
