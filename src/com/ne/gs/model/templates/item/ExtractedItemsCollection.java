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
import javax.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlType(name = "ExtractedItemsCollection")
public class ExtractedItemsCollection extends ResultedItemsCollection {

    @XmlAttribute(name = "chance")
    protected float chance = 100;
    @XmlAttribute(name = "minlevel")
    protected int minLevel;
    @XmlAttribute(name = "maxlevel")
    protected int maxLevel;

    public final float getChance() {
        return chance;
    }

    public final int getMinLevel() {
        return minLevel;
    }

    public final int getMaxLevel() {
        return maxLevel;
    }

}
