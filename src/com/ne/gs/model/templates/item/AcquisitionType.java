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

@XmlType(name = "acquisitionType")
@XmlEnum
public enum AcquisitionType {
    AP(0),
    ABYSS(1),
    REWARD(2),
    COUPON(2);

    private final int id;

    private AcquisitionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
