/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.attack;

import com.ne.gs.model.gameobjects.AionObject;

/**
 * AggroInfo: - hate of creature - damage of creature
 *
 * @author ATracer, Sarynth
 */
public class AggroInfo {

    private final AionObject attacker;
    private int hate;
    private int damage;

    /**
     * @param attacker
     */
    AggroInfo(AionObject attacker) {
        this.attacker = attacker;
    }

    /**
     * @return attacker
     */
    public AionObject getAttacker() {
        return attacker;
    }

    /**
     * @param damage
     */
    public void addDamage(int damage) {
        this.damage += damage;
        if (this.damage < 0) {
            this.damage = 0;
        }
    }

    /**
     * @param damage
     */
    public void addHate(int damage) {
        hate += damage;
        if (hate < 1) {
            hate = 1;
        }
    }

    /**
     * @return hate
     */
    public int getHate() {
        return hate;
    }

    /**
     * @param hate
     */
    public void setHate(int hate) {
        this.hate = hate;
    }

    /**
     * @return damage
     */
    public int getDamage() {
        return damage;
    }

    /**
     * @param damage
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }
}
