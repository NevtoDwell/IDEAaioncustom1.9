/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.tradelist;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author namedrisk
 */
@XmlType(name = "npc_type")
@XmlEnum
public enum TradeNpcType {
    NORMAL(1),
    ABYSS(2),
    REWARD(4);

    private final int index;

    private TradeNpcType(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
