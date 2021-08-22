/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "quality")
@XmlEnum
public enum ItemQuality {
    // TODO: Reorder and rename - requires ATracer parser update (?)

    JUNK(0),
    // Junk - Gray
    COMMON(1),
    // Common - White
    RARE(2),
    // Superior - Green
    LEGEND(3),
    // Heroic - Blue
    UNIQUE(4),
    // Fabled - Yellow
    EPIC(5),
    // Eternal - Orange
    MYTHIC(6);    // Test - Purple

    private final int qualityId;

    /**
     * Constructors
     */
    private ItemQuality(int qualityId) {
        this.qualityId = qualityId;
    }

    /**
     * Accessors
     */
    public int getQualityId() {
        return qualityId;
    }

}
