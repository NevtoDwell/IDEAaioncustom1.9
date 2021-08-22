/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.common.service;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Predicate;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RewardType;
import com.ne.gs.model.gameobjects.player.XPCape;
import com.ne.gs.model.team2.TemporaryPlayerTeam;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.abyss.AbyssPointsService;
import com.ne.gs.services.custom.CustomQuestsService;
import com.ne.gs.services.drop.DropRegistrationService;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.stats.StatFunctions;

/**
 * @author ATracer
 */
public final class PlayerTeamDistributionService {

    /**
     * This method will send a reward if a player is in a group
     */
    public static void doReward(TemporaryPlayerTeam<?> team, Npc owner) {
        if (team == null || owner == null) {
            return;
        }

        PlayerTeamRewardStats filteredStats = new PlayerTeamRewardStats(owner);
        team.applyOnMembers(filteredStats);

        // All are dead or not nearby.
        if (filteredStats.players.size() == 0 || !filteredStats.hasLivingPlayer) {
            return;
        }

        // Rewarding...
        long expReward;
        if (filteredStats.players.size() + filteredStats.mentorCount == 1) {
            expReward = StatFunctions.calculateSoloExperienceReward(filteredStats.players.get(0), owner);
        } else {
            expReward = StatFunctions.calculateGroupExperienceReward(filteredStats.highestLevel, owner);
        }

        // Party Bonus 2 members 10%, 3 members 20% ... 6 members 50%
        int size = filteredStats.players.size();
        int bonus = 100;
        if (size > 1) {
            bonus = 150 + (size - 2) * 10;
        }

        for (Player member : filteredStats.players) {
            CustomQuestsService.getInstance().onNpcKill(member, owner);
            if (!member.isMentor() && !member.getLifeStats().isAlreadyDead()) {
                // Exp reward
                long reward = expReward * bonus * member.getLevel() / (filteredStats.partyLvlSum * 100);

                // Players 10 levels below highest member get 0 exp.
                if (filteredStats.highestLevel - member.getLevel() >= 10) {
                    reward = 0;
                } else if (filteredStats.mentorCount > 0) {
                    int cape = XPCape.values()[member.getLevel()].value();
                    if (cape < reward) {
                        reward = cape;
                    }
                }

                member.getCommonData().addExp(reward, RewardType.GROUP_HUNTING, owner.getObjectTemplate().getNameId());

                // DP reward
                member.getLifeStats().increaseDp(StatFunctions.calculateGroupDPReward(member, owner));

                // AP reward
                if (owner.isRewardAP() && (filteredStats.mentorCount <= 0 || !CustomConfig.MENTOR_GROUP_AP)) {
                    int ap = StatFunctions.calculatePvEApGained(member, owner) / filteredStats.players.size();
                    AbyssPointsService.addAp(member, owner, ap, PlayerTeamDistributionService.class);
                }
            }
        }

        Player mostDamagePlayer = owner.getAggroList().getMostPlayerDamageOfMembers(team.getMembers(), filteredStats.highestLevel);
        if (mostDamagePlayer == null) {
            return;
        }
        if (!owner.getAi2().getName().equals("chest") || filteredStats.mentorCount == 0) {
            DropRegistrationService.getInstance().registerDrop(owner, mostDamagePlayer, filteredStats.highestLevel, filteredStats.players);
        }
    }

    private static class PlayerTeamRewardStats implements Predicate<Player> {

        final List<Player> players = new ArrayList<>();
        int partyLvlSum = 0;
        int highestLevel = 0;
        int mentorCount = 0;
        boolean hasLivingPlayer = false;
        Npc owner;

        public PlayerTeamRewardStats(Npc owner) {
            this.owner = owner;
        }

        @Override
        public boolean apply(Player member) {
            if (member.isOnline()) {
                if (MathUtil.isIn3dRange(member, owner, GroupConfig.GROUP_MAX_DISTANCE)) {
                    QuestEngine.getInstance().onKill(new QuestEnv(owner, member, 0, 0));
                    if (member.isMentor()) {
                        mentorCount++;
                        return true;
                    }
                    if (!hasLivingPlayer && !member.getLifeStats().isAlreadyDead()) {
                        hasLivingPlayer = true;
                    }
                    players.add(member);
                    partyLvlSum += member.getLevel();
                    if (member.getLevel() > highestLevel) {
                        highestLevel = member.getLevel();
                    }
                }
            }
            return true;
        }
    }
}
