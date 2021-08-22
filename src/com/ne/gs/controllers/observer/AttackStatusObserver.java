/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

import com.ne.gs.controllers.attack.AttackStatus;

/**
 * @author ATracer
 */
public class AttackStatusObserver extends AttackCalcObserver {

    protected int value;
    protected AttackStatus status;

    /**
     * @param value
     * @param status
     */
    public AttackStatusObserver(int value, AttackStatus status) {
        this.value = value;
        this.status = status;
    }
}
