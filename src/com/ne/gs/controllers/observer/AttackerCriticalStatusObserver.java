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

public class AttackerCriticalStatusObserver extends AttackCalcObserver {

    protected AttackerCriticalStatus acStatus = null;
    protected AttackStatus status;

    public AttackerCriticalStatusObserver(AttackStatus status, int count, int value, boolean isPercent) {
        this.status = status;
        acStatus = new AttackerCriticalStatus(count, value, isPercent);
    }

    public int getCount() {
        return acStatus.getCount();
    }

    public void decreaseCount() {
        acStatus.setCount(acStatus.getCount() - 1);
    }
}
