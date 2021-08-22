/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

import java.util.List;

import com.ne.gs.controllers.attack.AttackResult;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.model.gameobjects.Creature;

/**
 * @author ATracer
 */
public class AttackCalcObserver {

    /**
     * @param status
     *
     * @return false
     */
    public boolean checkStatus(AttackStatus status) {
        return false;
    }

    /**
     * @param attackList
     * @param attacker
     *
     * @return value
     */
    public void checkShield(List<AttackResult> attackList, Creature attacker) {

    }

    /**
     * @param status
     *
     * @return
     */
    public boolean checkAttackerStatus(AttackStatus status) {
        return false;
    }

    public AttackerCriticalStatus checkAttackerCriticalStatus(AttackStatus status, boolean isSkill) {
        return new AttackerCriticalStatus(false);
    }

    /**
     * @param isSkill
     *
     * @return physical damage multiplier
     */
    public float getBasePhysicalDamageMultiplier(boolean isSkill) {
        return 1f;
    }

    /**
     * @return magic damage multiplier
     */
    public float getBaseMagicalDamageMultiplier() {
        return 1f;
    }
}
