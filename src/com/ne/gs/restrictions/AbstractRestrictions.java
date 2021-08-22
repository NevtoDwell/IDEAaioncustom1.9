/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.restrictions;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author lord_rex
 */
public abstract class AbstractRestrictions implements Restrictions {

    public void activate() {
        RestrictionsManager.activate(this);
    }

    public void deactivate() {
        RestrictionsManager.deactivate(this);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * To avoid accidentally multiple times activated restrictions.
     */
    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    @DisabledRestriction
    public boolean isRestricted(Player player, Class<? extends Restrictions> callingRestriction) {
        throw new AbstractMethodError();
    }

    @Override
    @DisabledRestriction
    public boolean canAttack(Player player, VisibleObject target) {
        throw new AbstractMethodError();
    }

    @Override
    @DisabledRestriction
    public boolean canAffectBySkill(Player player, VisibleObject target, Skill skill) {
        throw new AbstractMethodError();
    }

    @Override
    @DisabledRestriction
    public boolean canUseSkill(Player player, Skill skill) {
        throw new AbstractMethodError();
    }

    @Override
    @DisabledRestriction
    public boolean canChat(Player player) {
        throw new AbstractMethodError();
    }

    @Override
    @DisabledRestriction
    public boolean canInviteToGroup(Player player, Player target) {
        throw new AbstractMethodError();
    }

    @Override
    @DisabledRestriction
    public boolean canChangeEquip(Player player) {
        throw new AbstractMethodError();
    }

    @Override
    @DisabledRestriction
    public boolean canUseWarehouse(Player player) {
        throw new AbstractMethodError();
    }

    @Override
    @DisabledRestriction
    public boolean canTrade(Player player) {
        throw new AbstractMethodError();
    }

    @Override
    @DisabledRestriction
    public boolean canUseItem(Player player, Item item) {
        throw new AbstractMethodError();
    }

}
