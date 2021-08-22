/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author Mr. Poke
 */
@XmlEnum
public enum ConditionUnionType {

    AND,
    OR;

    public String value() {
        return name();
    }

    public static ConditionUnionType fromValue(String v) {
        return valueOf(v);
    }

}
