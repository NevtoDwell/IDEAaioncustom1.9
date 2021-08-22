/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.event;

/**
 * @author ATracer
 */
public enum AIEventType {
    ACTIVATE,
    DEACTIVATE,
    FREEZE,
    UNFREEZE,
    /**
     * Creature is being attacked (internal)
     */
    ATTACK,
    /**
     * Creature's attack part is complete (internal)
     */
    ATTACK_COMPLETE,
    /**
     * Creature's stopping attack (internal)
     */
    ATTACK_FINISH,
    /**
     * Some neighbor creature is being attacked (broadcast)
     */
    CREATURE_NEEDS_SUPPORT,

    /**
     * Creature is attacking (broadcast)
     */

    MOVE_VALIDATE,
    MOVE_ARRIVED,

    CREATURE_SEE,
    CREATURE_NOT_SEE,
    CREATURE_MOVED,
    CREATURE_AGGRO,
    SPAWNED,
    RESPAWNED,
    DESPAWNED,
    DIED,

    TARGET_REACHED,
    TARGET_TOOFAR,
    TARGET_GIVEUP,
    TARGET_CHANGED,
    FOLLOW_ME,
    STOP_FOLLOW_ME,

    NOT_AT_HOME,
    BACK_HOME,

    DIALOG_START,
    DIALOG_FINISH,

    DROP_REGISTERED;
}
