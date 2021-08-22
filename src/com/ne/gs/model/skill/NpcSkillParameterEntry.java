/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.skill;

class NpcSkillParameterEntry extends NpcSkillEntry {

    public NpcSkillParameterEntry(int skillId, int skillLevel) {
        super(skillId, skillLevel);
    }

    @Override
    public boolean isReady(int hpPercentage, long fightingTimeInMSec) {
        return true;
    }

    @Override
    public boolean chanceReady() {
        return true;
    }

    @Override
    public boolean hpReady(int hpPercentage) {
        return true;
    }

    @Override
    public boolean timeReady(long fightingTimeInMSec) {
        return true;
    }

    @Override
    public boolean hasCooldown() {
        return false;
    }
}
