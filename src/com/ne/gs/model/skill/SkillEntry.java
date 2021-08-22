/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.skill;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
public abstract class SkillEntry {

    protected final int skillId;

    protected int skillLevel;

    SkillEntry(int skillId, int skillLevel) {
        this.skillId = skillId;
        this.skillLevel = skillLevel;
    }

    public final int getSkillId() {
        return skillId;
    }

    public final int getSkillLevel() {
        return skillLevel;
    }

    public final String getSkillName() {
        return DataManager.SKILL_DATA.getSkillTemplate(getSkillId()).getName();
    }

    public void setSkillLvl(int skillLevel) {
        this.skillLevel = skillLevel;
    }

    public final SkillTemplate getSkillTemplate() {
        return DataManager.SKILL_DATA.getSkillTemplate(getSkillId());
    }

}
