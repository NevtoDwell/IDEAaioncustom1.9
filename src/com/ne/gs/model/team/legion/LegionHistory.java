/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team.legion;

import java.sql.Timestamp;

/**
 * @author Simple
 */
public class LegionHistory {

    private final LegionHistoryType legionHistoryType;
    private final String name;
    private final Timestamp time;
    private final int tabId;
    private final String description;

    public LegionHistory(LegionHistoryType legionHistoryType, String name, Timestamp time, int tabId, String description) {
        this.legionHistoryType = legionHistoryType;
        this.name = name;
        this.time = time;
        this.tabId = tabId;
        this.description = description;
    }

    /**
     * @return the legionHistoryType
     */
    public LegionHistoryType getLegionHistoryType() {
        return legionHistoryType;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the time
     */
    public Timestamp getTime() {
        return time;
    }

    public int getTabId() {
        return tabId;
    }

    public String getDescription() {
        return description;
    }
}
