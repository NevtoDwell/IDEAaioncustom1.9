/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

/**
 * @author ATracer
 */
public enum TaskId {
    DECAY,
    RESPAWN,
    PRISON,
    PROTECTION_ACTIVE,
    DROWN,
    DESPAWN,
    /**
     * Quest task with timer
     */
    QUEST_TIMER,
    /**
     * Follow task checker
     */
    QUEST_FOLLOW,
    PLAYER_UPDATE,
    INVENTORY_UPDATE,
    GAG,
    ITEM_USE,
    ACTION_ITEM_NPC,
    HOUSE_OBJECT_USE,
    EXPRESS_MAIL_USE,
    SKILL_USE,
    GATHERABLE,
    PET_UPDATE,
    MATERIAL_ACTION,
    SUMMON_FOLLOW;
}
