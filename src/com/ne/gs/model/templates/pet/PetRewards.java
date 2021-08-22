/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.pet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetRewards", propOrder = {"results"})
public class PetRewards {

    @XmlElement(name = "result")
    protected List<PetFeedResult> results;

    @XmlAttribute(name = "group", required = true)
    protected FoodType type;

    @XmlAttribute
    protected boolean loved = false;

    public List<PetFeedResult> getResults() {
        if (results == null) {
            results = new ArrayList<>();
        }
        return results;
    }

    public FoodType getType() {
        return type;
    }

    public boolean isLoved() {
        return loved;
    }
}
