/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

public class AttackerCriticalStatus {

    private boolean result = false;
    private int count;
    private int value;
    private boolean isPercent;

    public AttackerCriticalStatus(boolean result) {
        this.result = result;
    }

    public AttackerCriticalStatus(int count, int value, boolean isPercent) {
        this.count = count;
        this.value = value;
        this.isPercent = isPercent;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getValue() {
        return value;
    }

    public boolean isPercent() {
        return isPercent;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
