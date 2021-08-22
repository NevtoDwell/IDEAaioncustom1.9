/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.skill;

/**
 * @author ATracer
 */
public abstract class NpcSkillEntry extends SkillEntry {

    protected long lastTimeUsed = 0;

    public NpcSkillEntry(int skillId, int skillLevel) {
        super(skillId, skillLevel);
    }

    public abstract boolean isReady(int paramInt, long paramLong);

    public abstract boolean chanceReady();

    public abstract boolean hpReady(int hpPercentage);

    public abstract boolean timeReady(long paramLong);

    public abstract boolean hasCooldown();

    public long getLastTimeUsed() {
        return lastTimeUsed;
    }

    public void setLastTimeUsed() {
        lastTimeUsed = System.currentTimeMillis();
    }
}
