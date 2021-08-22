/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.npcskill;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ConjunctionType")
@XmlEnum
public enum ConjunctionType {
    AND,
    OR,
    XOR;

    public String value() {
        return name();
    }

    public static ConjunctionType fromValue(String v) {
        return valueOf(v);
    }
}
