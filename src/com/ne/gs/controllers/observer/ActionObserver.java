/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
public class ActionObserver {

    private AtomicBoolean used;

    private final ObserverType observerType;

    public ActionObserver(ObserverType observerType) {
        this.observerType = observerType;
    }

    /**
     * Make this observer usable exactly one time
     */
    public void makeOneTimeUse() {
        used = new AtomicBoolean(false);
    }

    /**
     * Try to use this observer. Will return true only once.
     *
     * @return
     */
    public boolean tryUse() {
        return used.compareAndSet(false, true);
    }

    /**
     * @return the observerType
     */
    public ObserverType getObserverType() {
        return observerType;
    }

    public void moved() {
    }

    ;

    /**
     * @param creature
     */
    public void attacked(Creature creature) {
    }

    ;

    /**
     * @param creature
     */
    public void attack(Creature creature) {
    }

    ;

    /**
     * @param item
     * @param owner
     */
    public void equip(Item item, Player owner) {
    }

    ;

    /**
     * @param item
     * @param owner
     */
    public void unequip(Item item, Player owner) {
    }

    ;

    /**
     * @param skill
     */
    public void skilluse(Skill skill) {
    }

    ;

    /**
     * @param creature
     */
    public void died(Creature creature) {
    }

    ;

    /**
     * @param creature
     * @param dotEffect
     */
    public void dotattacked(Creature creature, Effect dotEffect) {
    }

    ;

    /**
     * @param item
     */
    public void itemused(Item item) {
    }

    /**
     * @param npc
     */
    public void npcdialogrequested(Npc npc) {
    }

    public void abnormalsetted(AbnormalState state) {
    }

    public void summonrelease() {
    }

    public void loot() {
    }

    public void gather() {
    }
}
