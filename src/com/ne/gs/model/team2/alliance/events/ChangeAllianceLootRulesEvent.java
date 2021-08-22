/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance.events;

import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_INFO;

/**
 * @author ATracer
 */
public class ChangeAllianceLootRulesEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

    private final PlayerAlliance alliance;
    private final LootGroupRules lootGroupRules;

    public ChangeAllianceLootRulesEvent(PlayerAlliance alliance, LootGroupRules lootGroupRules) {
        this.alliance = alliance;
        this.lootGroupRules = lootGroupRules;
    }

    @Override
    public void handleEvent() {
        alliance.setLootGroupRules(lootGroupRules);
        alliance.applyOnMembers(this);
    }

    @Override
    public boolean apply(Player member) {
        member.sendPck(new SM_ALLIANCE_INFO(alliance));
        return true;
    }

}
