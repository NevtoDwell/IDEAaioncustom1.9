/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.zone;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * @author MrPoke
 */
@XmlType(name = "ZoneClassName")
@XmlEnum
public enum ZoneClassName {
    DUMMY,
    SUB,
    FLY,
    ARTIFACT,
    FORT,
    LIMIT,
    ITEM_USE,
    PVP,
    DUEL,
    WEATHER,
    @Deprecated HOUSE,
    NEUTRAL,
    DOMINION,
    RACE,
    FFA,
    TVT,
    PEACE;
}
