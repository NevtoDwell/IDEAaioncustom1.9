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
 * @author ATracer
 */
@XmlType(name = "TargetSlot")
@XmlEnum
public enum SkillTargetSlot {
    BUFF,
    DEBUFF,
    CHANT,
    SPEC,
    SPEC2,
    BOOST,
    NOSHOW,
    NONE
}
