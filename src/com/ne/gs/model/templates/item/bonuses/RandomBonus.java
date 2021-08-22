/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.bonuses;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.templates.stats.ModifiersTemplate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RandomBonus", propOrder = {"modifiers"})
public class RandomBonus {

    @XmlElement(required = true)
    protected List<ModifiersTemplate> modifiers;

    @XmlAttribute(required = true)
    protected int id;

    public List<ModifiersTemplate> getModifiers() {
        if (modifiers == null) {
            modifiers = new ArrayList<>();
        }
        return modifiers;
    }

    public int getId() {
        return id;
    }
}
