/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2014, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.skill;

import org.apache.commons.lang3.ArrayUtils;

/**
 * This class ...
 *
 * @author hex1r0
 */
public enum SkillId {
    TransformationGuardianGeneral(
        8552, 8553, 8554, 8555, 8556, 8557, 8558, 8559, 8560, 8561,
        11885, 11886, 11887, 11888, 11889, 11890, 11891, 11892, 11893, 11894,
        11907, 11908, 11909, 11910, 11911, 11912, 11913, 11914, 11915, 11916
    ),

    RuneCarve(
        8303, 8304, 8305, 8306, 8307
    );

    public final int[] skillIds;

    SkillId(int... skillIds) {
        this.skillIds = skillIds;
    }

    public boolean contains(int skillId) {
        return ArrayUtils.contains(skillIds, skillId);
    }
}
