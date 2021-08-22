/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.quest;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlType(name = "QuestCategory")
@XmlEnum
public enum QuestCategory {
    QUEST(0),
    EVENT(1),
    MISSION(0),
    SIGNIFICANT(0),
    IMPORTANT(0),
    NON_COUNT(0),
    SEEN_MARKER(0),
    TASK(0),
    FACTION(0);

    private final int id;

    private QuestCategory(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
