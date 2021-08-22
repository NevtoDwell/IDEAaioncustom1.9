/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.common.legacy.LootDistribution;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.model.team2.common.legacy.LootRuleType;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * @author Lyahim, Simple, xTz
 */
public class CM_DISTRIBUTION_SETTINGS extends AionClientPacket {

    @SuppressWarnings("unused")
    private int unk1;
    private int lootrul;
    private int misc;
    private LootRuleType lootrules;
    private LootDistribution autodistribution;
    private int common_item_above;
    private int superior_item_above;
    private int heroic_item_above;
    private int fabled_item_above;
    private int ethernal_item_above;
    @SuppressWarnings("unused")
    private int unk2;
    private int autodistr;

    @Override
    protected void readImpl() {
        unk1 = readD();
        lootrul = readD();
        switch (lootrul) {
            case 0:
                lootrules = LootRuleType.FREEFORALL;
                break;
            case 1:
                lootrules = LootRuleType.ROUNDROBIN;
                break;
            case 2:
                lootrules = LootRuleType.LEADER;
                break;
            default:
                lootrules = LootRuleType.FREEFORALL;
                break;
        }
        misc = readD();
        common_item_above = readD();
        superior_item_above = readD();
        heroic_item_above = readD();
        fabled_item_above = readD();
        ethernal_item_above = readD();
        autodistr = readD();
        unk2 = readD();

        switch (autodistr) {
            case 0:
                autodistribution = LootDistribution.NORMAL;
                break;
            case 2:
                autodistribution = LootDistribution.ROLL_DICE;
                break;
            case 3:
                autodistribution = LootDistribution.BID;
                break;
            default:
                autodistribution = LootDistribution.NORMAL;
                break;
        }
    }

    @Override
    protected void runImpl() {
        Player leader = getConnection().getActivePlayer();

        PlayerGroup group = leader.getPlayerGroup2();
        if (group != null) {
            PlayerGroupService.changeGroupRules(group, new LootGroupRules(lootrules, autodistribution, common_item_above, superior_item_above,
                heroic_item_above, fabled_item_above, ethernal_item_above, misc));
        }
        com.ne.gs.model.team2.alliance.PlayerAlliance alliance = leader.getPlayerAlliance2();
        if (alliance != null) {
            PlayerAllianceService.changeGroupRules(alliance, new LootGroupRules(lootrules, autodistribution, common_item_above, superior_item_above,
                heroic_item_above, fabled_item_above, ethernal_item_above, misc));
        }
    }
}
