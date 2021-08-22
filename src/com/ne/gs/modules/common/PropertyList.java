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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.utils.xml.ElementList;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "PropertyList")
public class PropertyList extends ElementList<Property> {

    @XmlElement(name = "property")
    public List<Property> getProperties() {
        return getElements();
    }

    @Nullable
    public String getValue(String name) {
        for (Property property : this) {
            if (property.getName().equals(name)) {
                return property.getValue();
            }
        }

        return null;
    }

    @NotNull
    public String getValue(String name, String defaultValue) {
        for (Property property : this) {
            if (property.getName().equals(name)) {
                return property.getValue();
            }
        }

        return defaultValue;
    }
}
