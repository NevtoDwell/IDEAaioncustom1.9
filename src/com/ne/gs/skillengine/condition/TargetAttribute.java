/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.condition;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "TargetAttribute")
@XmlEnum
public enum TargetAttribute {
    NPC,
    PC,
    ALL,
    SELF,
    NONE;

    public String value() {
        return name();
    }

    public static TargetAttribute fromValue(String v) {
        return valueOf(v);
    }

}
