/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.skill;

import com.ne.gs.model.gameobjects.Creature;

/**
 * @author ATracer
 */
public interface SkillList<T extends Creature> {

    /**
     * Add skill to list
     *
     * @return true if operation was successful
     */
    boolean addSkill(T creature, int skillId, int skillLevel);

    /**
     * Remove skill from list
     *
     * @return true if operation was successful
     */
    boolean removeSkill(int skillId);

    /**
     * Check whether skill is present in list
     */
    boolean isSkillPresent(int skillId);

    int getSkillLevel(int skillId);

    /**
     * Size of skill list
     */
    int size();

}
