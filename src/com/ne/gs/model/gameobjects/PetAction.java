/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ATracer
 */
public enum PetAction {
    ADOPT(1),
    SURRENDER(2),
    SPAWN(3),
    DISMISS(4),
    TALK_WITH_MERCHANT(6),
    TALK_WITH_MINDER(7),
    FOOD(9),
    RENAME(10),
    MOOD(12),
    UNKNOWN(255);

    private static final TIntObjectHashMap<PetAction> petActions;

    static {
        petActions = new TIntObjectHashMap<>();
        for (PetAction action : values()) {
            petActions.put(action.getActionId(), action);
        }
    }

    private final int actionId;

    private PetAction(int actionId) {
        this.actionId = actionId;
    }

    public int getActionId() {
        return actionId;
    }

    public static PetAction getActionById(int actionId) {
        PetAction action = petActions.get(actionId);
        return action != null ? action : UNKNOWN;
    }
}
