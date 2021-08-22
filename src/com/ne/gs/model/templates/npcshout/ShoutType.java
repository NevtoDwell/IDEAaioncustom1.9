/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.npcshout;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */

@XmlType(name = "ShoutType")
@XmlEnum
public enum ShoutType {

    BROADCAST,
    SAY,
    HEAR;

    public String value() {
        return name();
    }

    public static ShoutType fromValue(String v) {
        return valueOf(v);
    }

}
