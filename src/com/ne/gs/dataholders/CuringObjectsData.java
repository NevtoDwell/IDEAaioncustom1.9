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

import com.ne.gs.model.templates.curingzones.CuringTemplate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"curingObject"})
@XmlRootElement(name = "curing_objects")
public class CuringObjectsData {

    @XmlElement(name = "curing_object")
    protected List<CuringTemplate> curingObject;

    @XmlTransient
    private final List<CuringTemplate> curingObjects = new ArrayList<>();

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        for (CuringTemplate template : curingObject) {
            curingObjects.add(template);
        }

        curingObject = null;
    }

    public int size() {
        return curingObjects.size();
    }

    public List<CuringTemplate> getCuringObject() {
        return curingObjects;
    }
}
