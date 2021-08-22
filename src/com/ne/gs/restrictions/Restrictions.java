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
public interface Restrictions {

    public boolean isRestricted(Player player, Class<? extends Restrictions> callingRestriction);

    public boolean canAttack(Player player, VisibleObject target);

    public boolean canAffectBySkill(Player player, VisibleObject target, Skill skill);

    public boolean canUseSkill(Player player, Skill skill);

    public boolean canChat(Player player);

    public boolean canInviteToGroup(Player player, Player target);

    public boolean canInviteToAlliance(Player player, Player target);

    public boolean canChangeEquip(Player player);

    public boolean canUseWarehouse(Player player);

    public boolean canTrade(Player player);

    public boolean canUseItem(Player player, Item item);
}
