/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author MrPoke
 */
public abstract class ItemUseObserver extends ActionObserver {

    /**
     */
    public ItemUseObserver() {
        super(ObserverType.ALL);
    }

    @Override
    public final void attack(Creature creature) {
        abort();
    }

    @Override
    public final void attacked(Creature creature) {
        abort();
    }

    @Override
    public final void died(Creature creature) {
        abort();
    }

    @Override
    public final void dotattacked(Creature creature, Effect dotEffect) {
        if(dotEffect.getSkillId() == 50056){
        }
        else{                                       
        abort();
        }
    }

    @Override
    public final void equip(Item item, Player owner) {
        abort();
    }

    @Override
    public final void moved() {
        abort();
    }

    @Override
    public final void skilluse(Skill skill) {
        abort();
    }

    public abstract void abort();
}
