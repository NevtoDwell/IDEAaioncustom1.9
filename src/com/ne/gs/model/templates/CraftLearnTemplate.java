/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates;

public class CraftLearnTemplate {

    private final int skillId;
    private final boolean isCraftSkill;

    public boolean isCraftSkill() {
        return isCraftSkill;
    }

    public CraftLearnTemplate(int skillId, boolean isCraftSkill, String skillName) {
        this.skillId = skillId;
        this.isCraftSkill = isCraftSkill;
    }

    public int getSkillId() {
        return skillId;
    }
}
