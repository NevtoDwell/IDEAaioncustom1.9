/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.restrictions;

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author lord_rex
 */
public class ShutdownRestrictions extends AbstractRestrictions {

    @Override
    public boolean isRestricted(Player player, Class<? extends Restrictions> callingRestriction) {
        if (isInShutdownProgress(player)) {
            player.sendMsg("You are in shutdown progress!");
            return true;
        }

        return false;
    }

    @Override
    public boolean canAttack(Player player, VisibleObject target) {
        if (isInShutdownProgress(player)) {
            player.sendMsg("You cannot attack in Shutdown progress!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canAffectBySkill(Player player, VisibleObject target, Skill skill) {
        return true;
    }

    @Override
    public boolean canUseSkill(Player player, Skill skill) {
        if (isInShutdownProgress(player)) {
            player.sendMsg("You cannot use skills in Shutdown progress!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canChat(Player player) {
        if (isInShutdownProgress(player)) {
            player.sendMsg("You cannot chat in Shutdown progress!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canInviteToGroup(Player player, Player target) {
        if (isInShutdownProgress(player)) {
            player.sendMsg("You cannot invite members to group in Shutdown progress!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canInviteToAlliance(Player player, Player target) {
        if (isInShutdownProgress(player)) {
            player.sendMsg("You cannot invite members to alliance in Shutdown progress!");
            return false;
        }

        return true;
    }

    @Override
    public boolean canChangeEquip(Player player) {
        if (isInShutdownProgress(player)) {
            player.sendMsg("You cannot equip / unequip item in Shutdown progress!");
            return false;
        }

        return true;
    }

    private boolean isInShutdownProgress(Player player) {
        return player.getController().isInShutdownProgress();
    }

}
