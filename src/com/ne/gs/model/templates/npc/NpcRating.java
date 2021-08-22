/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.npc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.state.CreatureSeeState;

/**
 * @author ATracer
 */
@XmlType(name = "rating")
@XmlEnum
public enum NpcRating {
    JUNK(CreatureSeeState.NORMAL),
    NORMAL(CreatureSeeState.NORMAL),
    ELITE(CreatureSeeState.SEARCH1),
    HERO(CreatureSeeState.SEARCH2),
    LEGENDARY(CreatureSeeState.SEARCH2);

    private final CreatureSeeState congenitalSeeState;

    private NpcRating(CreatureSeeState congenitalSeeState) {
        this.congenitalSeeState = congenitalSeeState;
    }

    public CreatureSeeState getCongenitalSeeState() {
        return congenitalSeeState;
    }
}
