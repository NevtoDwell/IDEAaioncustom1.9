/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

/**
 * @author ATracer
 */
public enum ObserverType {
    MOVE(1),
    ATTACK(1 << 1),
    ATTACKED(1 << 2),
    EQUIP(1 << 3),
    UNEQUIP(1 << 4),
    SKILLUSE(1 << 5),
    DEATH(1 << 6),
    DOT_ATTACKED(1 << 7),
    ITEMUSE(1 << 8),
    NPCDIALOGREQUEST(
        1 << 9),
    ABNORMALSETTED(1 << 10),
    SUMMONRELEASE(1 << 11),
    LOOT(1 << 12),
    GATHER(1 << 13),

    HIDE_CANCEL(ATTACK.observerMask | SKILLUSE.observerMask | LOOT.observerMask | GATHER.observerMask | ITEMUSE.observerMask | NPCDIALOGREQUEST.observerMask),

    EQUIP_UNEQUIP(EQUIP.observerMask | UNEQUIP.observerMask),
    ATTACK_DEFEND(
        ATTACK.observerMask | ATTACKED.observerMask),
    MOVE_OR_DIE(MOVE.observerMask | DEATH.observerMask),
    ALL(MOVE.observerMask | ATTACK.observerMask | ATTACKED.observerMask | SKILLUSE.observerMask
        | DEATH.observerMask | DOT_ATTACKED.observerMask | ITEMUSE.observerMask | NPCDIALOGREQUEST.observerMask | ABNORMALSETTED.observerMask
        | SUMMONRELEASE.observerMask);

    private final int observerMask;

    private ObserverType(int observerMask) {
        this.observerMask = observerMask;
    }

    public boolean matchesObserver(ObserverType observerType) {
        return (observerType.observerMask & observerMask) == observerType.observerMask;
    }
}
