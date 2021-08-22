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
 * @author MrPoke
 */

@XmlEnum
public enum QuestStatus {
    NONE(0),
    // Default status. Aborted quests and the quests, where the quest timer ended. Used for beginning a new
    // quest. Stored together with other quests in the player's quest list, so don't count them! Invisible
    // in the player's quest list
    START(3),
    // Accepted quests
    REWARD(4),
    // The quests, that are finished. "Go and get your reward"
    COMPLETE(5),
    // Completed quests
    LOCKED(6); // Not (yet) available quests

    private final int id;

    private QuestStatus(int id) {
        this.id = id;
    }

    public int value() {
        return id;
    }
}
