/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.attack;

import com.ne.gs.skillengine.model.HitType;

/**
 * @author ATracer modified by Sippolo, kecimis
 */
public class AttackResult {

    private int damage;

    private final AttackStatus attackStatus;

    private HitType hitType = HitType.EVERYHIT;

    /**
     * shield effects related
     */
    private int shieldType;
    private int reflectedDamage = 0;
    private int reflectedSkillId = 0;
    private int protectedSkillId = 0;
    private int protectedDamage = 0;
    private int protectorId = 0;

    private boolean launchSubEffect = true;

    public AttackResult(int damage, AttackStatus attackStatus) {
        this.damage = damage;
        this.attackStatus = attackStatus;
    }

    public AttackResult(int damage, AttackStatus attackStatus, HitType type) {
        this(damage, attackStatus);
        hitType = type;
    }

    /**
     * @return the damage
     */
    public int getDamage() {
        return damage;
    }

    /**
     * @param damage
     *     the damage to set
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * @return the attackStatus
     */
    public AttackStatus getAttackStatus() {
        return attackStatus;
    }

    /**
     * @return the Damage Type
     */
    public HitType getDamageType() {
        return hitType;
    }

    /**
     * @param type
     *     the Damage Type to set
     */
    public void setDamageType(HitType type) {
        hitType = type;
    }

    /**
     * shield effects related
     */

    /**
     * @return the shieldType
     */
    public int getShieldType() {
        return shieldType;
    }

    /**
     * @param shieldType
     *     the shieldType to set
     */
    public void setShieldType(int shieldType) {
        this.shieldType |= shieldType;
    }

    public int getReflectedDamage() {
        return reflectedDamage;
    }

    public void setReflectedDamage(int reflectedDamage) {
        this.reflectedDamage = reflectedDamage;
    }

    public int getReflectedSkillId() {
        return reflectedSkillId;
    }

    public void setReflectedSkillId(int skillId) {
        reflectedSkillId = skillId;
    }

    public int getProtectedSkillId() {
        return protectedSkillId;
    }

    public void setProtectedSkillId(int skillId) {
        protectedSkillId = skillId;
    }

    public int getProtectedDamage() {
        return protectedDamage;
    }

    public void setProtectedDamage(int protectedDamage) {
        this.protectedDamage = protectedDamage;
    }

    public int getProtectorId() {
        return protectorId;
    }

    public void setProtectorId(int protectorId) {
        this.protectorId = protectorId;
    }

    public boolean isLaunchSubEffect() {
        return launchSubEffect;
    }

    public void setLaunchSubEffect(boolean launchSubEffect) {
        this.launchSubEffect = launchSubEffect;
    }

}
