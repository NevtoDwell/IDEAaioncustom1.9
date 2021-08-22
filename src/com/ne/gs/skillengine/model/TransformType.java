/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author kecimis
 */

@XmlType(name = "TransformType")
@XmlEnum
public enum TransformType {
    NONE(0),
    PC(1),
    AVATAR(2),
    FORM1(3);

    private final int id;

    TransformType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
