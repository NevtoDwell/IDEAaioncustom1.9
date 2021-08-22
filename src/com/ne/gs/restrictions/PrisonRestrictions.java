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
import com.ne.gs.world.WorldMapType;

/**
 * @author lord_rex
 */
public class PrisonRestrictions extends AbstractRestrictions {

    @Override
    public boolean isRestricted(Player player, Class<? extends Restrictions> callingRestriction) {
        if (isInPrison(player)) {
            player.sendMsg("You are in prison!");
            return true;
        }

        return false;
    }

    @Override
    public boolean canAttack(Player player, VisibleObject target) {
        if (isInPrison(player)) {
            player.sendMsg("You cannot attack in prison!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canUseSkill(Player player, Skill skill) {
        if (isInPrison(player)) {
            player.sendMsg("You cannot use skills in prison!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canAffectBySkill(Player player, VisibleObject target, Skill skill) {
        return true;
    }

    @Override
    public boolean canChat(Player player) {
        if (isInPrison(player)) {
            player.sendMsg("You cannot chat in prison!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canInviteToGroup(Player player, Player target) {
        if (isInPrison(player)) {
            player.sendMsg("You cannot invite members to group in prison!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canInviteToAlliance(Player player, Player target) {
        if (isInPrison(player)) {
            player.sendMsg("You cannot invite members to alliance in prison!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canChangeEquip(Player player) {
        if (isInPrison(player)) {
            player.sendMsg("You cannot equip / unequip item in prison!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canUseItem(Player player, Item item) {
        if (isInPrison(player)) {
            player.sendMsg("You cannot use item in prison!");
            return false;
        }
        return true;
    }

    private boolean isInPrison(Player player) {
        return player.isInPrison() || player.getWorldId() == WorldMapType.DE_PRISON.getId() || player.getWorldId() == WorldMapType.DF_PRISON.getId();
    }
}
