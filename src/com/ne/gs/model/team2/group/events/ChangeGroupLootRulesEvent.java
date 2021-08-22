/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group.events;

import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_INFO;

/**
 * @author ATracer
 */
public class ChangeGroupLootRulesEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

    private final PlayerGroup group;
    private final LootGroupRules lootGroupRules;

    public ChangeGroupLootRulesEvent(PlayerGroup group, LootGroupRules lootGroupRules) {
        this.group = group;
        this.lootGroupRules = lootGroupRules;
    }

    @Override
    public boolean apply(Player member) {
        member.sendPck(new SM_GROUP_INFO(group));
        return true;
    }

    @Override
    public void handleEvent() {
        group.setLootGroupRules(lootGroupRules);
        group.applyOnMembers(this);
    }

}
