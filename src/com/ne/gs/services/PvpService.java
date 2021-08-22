/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.ArrayList;
import java.util.List;

import com.ne.gs.services.custom.CustomQuestsService;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.configs.main.PunishmentConfig;
import com.ne.gs.configs.main.PvPConfig;
import com.ne.gs.controllers.attack.AggroInfo;
import com.ne.gs.controllers.attack.KillList;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RewardType;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.abyss.AbyssPointsService;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.reward.RewardService;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.utils.stats.AbyssRankEnum;
import com.ne.gs.utils.stats.StatFunctions;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author Sarynth
 */
public final class PvpService {

    private static final Logger log = LoggerFactory.getLogger("KILL_LOG");

    private static final FastMap<Integer, KillList> frequentKills = new FastMap<Integer, KillList>().shared();
    private static final FastMap<Integer, KillList> dailyKills = new FastMap<Integer, KillList>().shared();

    public static int getKillsFor(int winnerId, int victimId) {
        KillList winnerKillList = frequentKills.get(winnerId);

        if (winnerKillList == null) {
            return 0;
        }
        return winnerKillList.getFrequentKillsFor(victimId);
    }

    private static void addKillFor(int winnerId, int victimId) {
        KillList winnerKillList = frequentKills.get(winnerId);
        if (winnerKillList == null) {
            winnerKillList = new KillList();
            frequentKills.put(winnerId, winnerKillList);
        }
        winnerKillList.addKillFor(victimId);
    }

    public static int getDailyKillsFor(int winnerId, int victimId) {
        KillList winnerKillList = dailyKills.get(winnerId);

        if (winnerKillList == null) {
            return 0;
        }
        return winnerKillList.getDailyKillsFor(victimId);
    }

    private static void addDailyKillFor(int winnerId, int victimId) {
        KillList winnerKillList = dailyKills.get(winnerId);
        if (winnerKillList == null) {
            winnerKillList = new KillList();
            dailyKills.put(winnerId, winnerKillList);
        }
        winnerKillList.addKillFor(victimId);
    }

    public static void doReward(final Player victim) {
        // [+] do not give AP to dueling players
        if (victim.isInState(CreatureState.DUELING)) {
            return;
        }

        // winner is the player that receives the kill count
        final Player winner = victim.getAggroList().getMostPlayerDamage();

        int totalDamage = victim.getAggroList().getTotalDamage();

        if (totalDamage == 0 || winner == null) {
            return;
        }

        // Add Player Kill to record.
        // Pvp Kill Reward.
        int reduceap = PunishmentConfig.PUNISHMENT_REDUCEAP;
        if (reduceap < 0) {
            reduceap *= -1;
        }
        if (reduceap > 100) {
            reduceap = 100;
        }

       // Announce that player has died to all world.
       World.getInstance().doOnAllPlayers(new Visitor<Player>() {
           @Override
           public void visit(Player player) {
               PacketSendUtility.sendWhiteMessage(player, "" + winner.getName() + " \u0443\u0431\u0438\u0432\u0430\u0435\u0442 \u043f\u0435\u0440\u0441\u043e\u043d\u0430\u0436\u0430 " + victim.getName());
           }
       });

        // Kill-log
        if (LoggingConfig.LOG_PL && reduceap > 0) {
            String ip1 = winner.getClientConnection().getIP();
            String mac1 = winner.getClientConnection().getMacAddress();
            String ip2 = victim.getClientConnection().getIP();
            String mac2 = victim.getClientConnection().getMacAddress();
            if ((mac1 != null) && (mac2 != null)) {
                if ((ip1.equalsIgnoreCase(ip2)) && (mac1.equalsIgnoreCase(mac2))) {
                    AuditLogger.info(winner, "Power Leveling : " + winner.getName() + " with " + victim.getName() + ", They have the sames ip=" + ip1
                        + " and mac=" + mac1 + ".");
                    if (reduceap > 0) {
                        int win_ap = winner.getAbyssRank().getAp() * reduceap / 100;
                        int vic_ap = victim.getAbyssRank().getAp() * reduceap / 100;
                        addAp(winner, winner,  win_ap);
                        addAp(victim, victim, -vic_ap);
                        winner.sendMsg("[PL-AP] You lost " + reduceap + "% of your total ap");
                        victim.sendMsg("[PL-AP] You lost " + reduceap + "% of your total ap");
                    }
                    return;
                }
                if (ip1.equalsIgnoreCase(ip2)) {
                    AuditLogger.info(winner, "Possible Power Leveling : " + winner.getName() + " with " + victim.getName() + ", They have the sames ip=" + ip1
                        + ".");
                    AuditLogger.info(winner, "Check if " + winner.getName() + " and " + victim.getName() + " are Brothers-Sisters-Lovers-dogs-cats...");
                }
            }
        }
        if (winner.getLevel() - victim.getLevel() <= PvPConfig.MAX_AUTHORIZED_LEVEL_DIFF) {
            int frequentKills = getKillsFor(winner.getObjectId(), victim.getObjectId());
            int dailyKills = getDailyKillsFor(winner.getObjectId(), victim.getObjectId());

            if (frequentKills != 0) {
                winner.sendMsg(String.format("Вы не получите награду за убийство %s чаще чем раз в %d сек!", victim.getName(), PvPConfig.CHAIN_KILL_TIME_RESTRICTION / 1000));
            } else if (dailyKills >= PvPConfig.CHAIN_KILL_NUMBER_RESTRICTION) {
                winner.sendMsg(String.format("Сегодня вы убили %s %d раз, и больше не получите награду!", victim.getName(), dailyKills));
            } else {
                if (PvPConfig.ENABLE_MEDAL_REWARDING) {
                    if (Rnd.get(100) < (int)PvPRewardService.getMedalRewardChance(winner, victim)) {
                        int medalId = PvPRewardService.getRewardId(winner, victim, false);
                        long medalCount = PvPRewardService.getRewardQuantity(winner, victim);
                        ItemTemplate tpl = DataManager.ITEM_DATA.getItemTemplate(medalId);
                        ItemService.addItem(winner, medalId, medalCount);
//                        if (winner.getInventory().getItemCountByItemId(medalId) > 0) {
//                            if (medalCount == 1) {
//                                winner.sendPck(new SM_SYSTEM_MESSAGE(1390000, DescId.of(tpl.getNameId()))); // You have aquired %s
//                            } else {
//                                winner.sendPck(new SM_SYSTEM_MESSAGE(1390005, medalCount, DescId.of(tpl.getNameId()))); // You have aquired %s %d(s)
//                            }
//                        }
                    }
                }
                if (PvPConfig.ENABLE_TOLL_REWARDING && Rnd.chance(PvPRewardService.getTollRewardChance(winner, victim))) {
                    int qt = PvPRewardService.getTollQuantity(winner, victim);
                    InGameShopEn.getInstance().addToll(winner, qt);
                    if (qt == 1) {
                        PacketSendUtility.sendBrightYellowMessage(winner, "Vous avez obtenu " + qt + " point boutique.");
                    } else {
                        PacketSendUtility.sendBrightYellowMessage(winner, "Vous avez obtenu " + qt + " points boutique.");
                    }
                }
                if (PvPConfig.GENOCIDE_SPECIAL_REWARDING != 0) {
                    switch (PvPConfig.GENOCIDE_SPECIAL_REWARDING) {
                        case 1:
                            if (winner.getSpreeLevel() <= 2 || !Rnd.chance(PvPConfig.SPECIAL_REWARD_CHANCE)) {
                                break;
                            }
                            int abyssId = PvPRewardService.getRewardId(winner, victim, true);
                            ItemService.addItem(winner, abyssId, 1L);
                            log.info("[PvP][Advanced] {Player : " + winner.getName() + "} has won " + abyssId + " for killing {Player : " + victim.getName()
                                + "}");
                            break;
                        default:
                            if (winner.getSpreeLevel() <= 2 || !Rnd.chance(PvPConfig.SPECIAL_REWARD_CHANCE)) {
                                break;
                            }
                            ItemService.addItem(winner, PvPConfig.GENOCIDE_SPECIAL_REWARDING, 1L);
                            log.info("[PvP][Advanced] {Player : " + winner.getName() + "} has won " + PvPConfig.GENOCIDE_SPECIAL_REWARDING
                                + " for killing {Player : " + victim.getName() + "}");
                            break;
                    }
                }
            }
        }
        int playerDamage = 0;
        boolean success = false;

        // Distribute AP to groups and players that had damage.
        for (AggroInfo aggro : victim.getAggroList().getFinalDamageList(true)) {
            if (aggro.getAttacker() instanceof Player) {
                success = rewardPlayer(victim, totalDamage, aggro);
            } else if (aggro.getAttacker() instanceof PlayerGroup) {
                success = rewardPlayerGroup(victim, totalDamage, aggro);
            } else if (aggro.getAttacker() instanceof PlayerAlliance) {
                success = rewardPlayerAlliance(victim, totalDamage, aggro);
            }

            // Add damage last, so we don't include damage from same race. (Duels, Arena)
            if (success) {
                playerDamage += aggro.getDamage();
            }
        }

        // Apply lost AP to defeated player
        int apLost = StatFunctions.calculatePvPApLost(victim, winner);
        int apActuallyLost = apLost * playerDamage / totalDamage;

        if (apActuallyLost > 0) {
            addAp(victim, victim, -apActuallyLost);
        }
        
        RewardService.getInstance().enemyKillReward(winner, victim);
        
 //       PacketSendUtility.broadcastPacketAndReceive(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH_TO_B(victim.getName(), winner.getName()));
        if (LoggingConfig.LOG_KILL) {
            log.info("[KILL] Player [" + winner.getName() + "] killed [" + victim.getName() + "]");
        }
    }

    private static void addAp(Player player, VisibleObject obj, int value) {
        AbyssPointsService.addAp(player, obj,  value, PvpService.class);
    }

    private static boolean canGainAp(Player winner, Player victim) {
        return getKillsFor(winner.getObjectId(), victim.getObjectId()) == 0
            && getDailyKillsFor(winner.getObjectId(), victim.getObjectId()) < PvPConfig.CHAIN_KILL_NUMBER_RESTRICTION;
    }

    /**
     * @param victim
     * @param totalDamage
     * @param aggro
     *
     * @return true if group is not same race
     */
    private static boolean rewardPlayerGroup(Player victim, int totalDamage, AggroInfo aggro) {
        // Reward Group
        PlayerGroup group = ((PlayerGroup) aggro.getAttacker());

        // Don't Reward Player of Same Faction.
        if (group.getRace() == victim.getRace()) {
            return false;
        }

        // Find group members in range
        List<Player> players = new ArrayList<>();

        // Find highest rank and level in local group
        int maxRank = AbyssRankEnum.GRADE9_SOLDIER.getId();
        int maxLevel = 0;

        for (Player member : group.getMembers()) {
            if (MathUtil.isIn3dRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE)) {
                // Don't distribute AP to a dead player!
                if (!member.getLifeStats().isAlreadyDead()) {
                    players.add(member);
                    if (member.getLevel() > maxLevel) {
                        maxLevel = member.getLevel();
                    }
                    if (member.getAbyssRank().getRank().getId() > maxRank) {
                        maxRank = member.getAbyssRank().getRank().getId();
                    }
                }
            }
        }

        // They are all dead or out of range.
        if (players.isEmpty()) {
            return false;
        }

        int baseApReward = StatFunctions.calculatePvpApGained(victim, maxRank, maxLevel);
        int baseXpReward = StatFunctions.calculatePvpXpGained(victim, maxRank, maxLevel);
        int baseDpReward = StatFunctions.calculatePvpDpGained(victim, maxRank, maxLevel);
        float groupPercentage = (float) aggro.getDamage() / totalDamage;
        int apRewardPerMember = Math.round(baseApReward * groupPercentage / players.size());
        int xpRewardPerMember = Math.round(baseXpReward * groupPercentage / players.size());
        int dpRewardPerMember = Math.round(baseDpReward * groupPercentage / players.size());

        for (Player member : players) {
            int memberApGain = 1;
            int memberXpGain = 1;
            int memberDpGain = 1;
            if (canGainAp(member, victim)) {
                if (apRewardPerMember > 0) {
                    memberApGain = Math.round(apRewardPerMember * member.getRates().getApPlayerGainRate());
                }
                if (xpRewardPerMember > 0) {
                    memberXpGain = Math.round(xpRewardPerMember * member.getRates().getXpPlayerGainRate());
                }
                if (dpRewardPerMember > 0) {
                    memberDpGain = Math.round(StatFunctions.adjustPvpDpGained(dpRewardPerMember, victim.getLevel(), member.getLevel())
                        * member.getRates().getDpPlayerRate());
                }
                member.getAbyssRank().updateKillCounts();

                if (PvPConfig.ENABLE_KILLING_SPREE_SYSTEM) {
                    Player luckyPlayer = players.get(Rnd.get(players.size()));
                    PvPSpreeService.increaseRawKillCount(luckyPlayer);
                }

                addAp(member, victim, memberApGain);
                member.getCommonData().addExp(memberXpGain, RewardType.PVP_KILL, victim.getName());
                member.getLifeStats().increaseDp(memberDpGain);

                addDailyKillFor(member.getObjectId(), victim.getObjectId()); // count only successfull kills
            }

            addKillFor(member.getObjectId(), victim.getObjectId());
            // notify Kill-Quests
            int worldId = member.getWorldId();
            QuestEngine.getInstance().onKillInWorld(new QuestEnv(victim, member, 0, 0), worldId);
            QuestEngine.getInstance().onKillRanked(new QuestEnv(victim, member, 0, 0), victim.getAbyssRank().getRank());
            CustomQuestsService.getInstance().onPlayerKill(member, victim);
        }

        return true;
    }

    /**
     * @param victim
     * @param totalDamage
     * @param aggro
     *
     * @return true if group is not same race
     */
    private static boolean rewardPlayerAlliance(Player victim, int totalDamage, AggroInfo aggro) {
        // Reward Alliance
        PlayerAlliance alliance = ((PlayerAlliance) aggro.getAttacker());

        // Don't Reward Player of Same Faction.
        if (alliance.getLeaderObject().getRace() == victim.getRace()) {
            return false;
        }

        // Find group members in range
        List<Player> players = new ArrayList<>();

        // Find highest rank and level in local group
        int maxRank = AbyssRankEnum.GRADE9_SOLDIER.getId();
        int maxLevel = 0;

        for (Player member : alliance.getMembers()) {
            if (!member.isOnline()) {
                continue;
            }
            if (MathUtil.isIn3dRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE)) {
                // Don't distribute AP to a dead player!
                if (!member.getLifeStats().isAlreadyDead()) {
                    players.add(member);
                    if (member.getLevel() > maxLevel) {
                        maxLevel = member.getLevel();
                    }
                    if (member.getAbyssRank().getRank().getId() > maxRank) {
                        maxRank = member.getAbyssRank().getRank().getId();
                    }
                }
            }
        }

        // They are all dead or out of range.
        if (players.isEmpty()) {
            return false;
        }

        int baseApReward = StatFunctions.calculatePvpApGained(victim, maxRank, maxLevel);
        int baseXpReward = StatFunctions.calculatePvpXpGained(victim, maxRank, maxLevel);
        int baseDpReward = StatFunctions.calculatePvpDpGained(victim, maxRank, maxLevel);
        float groupPercentage = (float) aggro.getDamage() / totalDamage;
        int apRewardPerMember = Math.round(baseApReward * groupPercentage / players.size());
        int xpRewardPerMember = Math.round(baseXpReward * groupPercentage / players.size());
        int dpRewardPerMember = Math.round(baseDpReward * groupPercentage / players.size());

        for (Player member : players) {
            int memberApGain = 1;
            int memberXpGain = 1;
            int memberDpGain = 1;
            if (canGainAp(member, victim)) {
                if (apRewardPerMember > 0) {
                    memberApGain = Math.round(apRewardPerMember * member.getRates().getApPlayerGainRate());
                }
                if (xpRewardPerMember > 0) {
                    memberXpGain = Math.round(xpRewardPerMember * member.getRates().getXpPlayerGainRate());
                }
                if (dpRewardPerMember > 0) {
                    memberDpGain = Math.round(StatFunctions.adjustPvpDpGained(dpRewardPerMember, victim.getLevel(), member.getLevel())
                        * member.getRates().getDpPlayerRate());
                }
                member.getAbyssRank().updateKillCounts();
                if (PvPConfig.ENABLE_KILLING_SPREE_SYSTEM) {
                    Player luckyPlayer = players.get(Rnd.get(players.size()));
                    PvPSpreeService.increaseRawKillCount(luckyPlayer);
                }

                addAp(member, victim, memberApGain);
                member.getCommonData().addExp(memberXpGain, RewardType.PVP_KILL, victim.getName());
                member.getLifeStats().increaseDp(memberDpGain);

                addDailyKillFor(member.getObjectId(), victim.getObjectId()); // count only successfull kills
            }

            addKillFor(member.getObjectId(), victim.getObjectId());
            // notify Kill-Quests
            int worldId = member.getWorldId();
            QuestEngine.getInstance().onKillInWorld(new QuestEnv(victim, member, 0, 0), worldId);
            QuestEngine.getInstance().onKillRanked(new QuestEnv(victim, member, 0, 0), victim.getAbyssRank().getRank());
            CustomQuestsService.getInstance().onPlayerKill(member, victim);
        }

        return true;
    }

    /**
     * @param victim
     * @param totalDamage
     * @param aggro
     *
     * @return true if player is not same race
     */
    private static boolean rewardPlayer(Player victim, int totalDamage, AggroInfo aggro) {
        // Reward Player
        Player winner = ((Player) aggro.getAttacker());

        if ((winner.getRace() == victim.getRace()) || (!MathUtil.isIn3dRange(winner, victim, GroupConfig.GROUP_MAX_DISTANCE))
            || (winner.getLifeStats().isAlreadyDead())) {
            return false;
        }

        if (canGainAp(winner, victim)) {
            int baseApReward = StatFunctions.calculatePvpApGained(victim, winner.getAbyssRank().getRank().getId(), winner.getLevel());
            int baseXpReward = StatFunctions.calculatePvpXpGained(victim, winner.getAbyssRank().getRank().getId(), winner.getLevel());
            int baseDpReward = StatFunctions.calculatePvpDpGained(victim, winner.getAbyssRank().getRank().getId(), winner.getLevel());
            winner.getAbyssRank().updateKillCounts();
            if (PvPConfig.ENABLE_KILLING_SPREE_SYSTEM) {
                PvPSpreeService.increaseRawKillCount(winner);
            }

            int apPlayerReward;
            int xpPlayerReward;
            int dpPlayerReward;

            if(aggro.getDamage() >= totalDamage - 1 && applyBoost(winner)){

                apPlayerReward = Math.round(baseApReward * winner.getRates().getApPlayerGainRate() * (Math.max(100, PvPConfig.SOLOBOOST_AP_PERC) / 100f));
                xpPlayerReward = Math.round(baseXpReward * winner.getRates().getXpPlayerGainRate() * (Math.max(100, PvPConfig.SOLOBOOST_XP_PERC) / 100f));
                dpPlayerReward = Math.round(baseDpReward * winner.getRates().getDpPlayerRate() * (Math.max(100, PvPConfig.SOLOBOOST_DP_PERC) / 100f));

                winner.sendMsg("За убийство " + victim.getName() + " в одиночку, вы получаете повышенную награду!");

            }else {

                apPlayerReward = Math.round(baseApReward * winner.getRates().getApPlayerGainRate() * aggro.getDamage() / totalDamage);
                xpPlayerReward = Math.round(baseXpReward * winner.getRates().getXpPlayerGainRate() * aggro.getDamage() / totalDamage);
                dpPlayerReward = Math.round(baseDpReward * winner.getRates().getDpPlayerRate() * aggro.getDamage() / totalDamage);
            }

            addAp(winner, victim, apPlayerReward);
            winner.getCommonData().addExp(xpPlayerReward, RewardType.PVP_KILL, victim.getName());
            winner.getLifeStats().increaseDp(dpPlayerReward);

            addDailyKillFor(winner.getObjectId(), victim.getObjectId()); // count only successfull kills
        }

        addKillFor(winner.getObjectId(), victim.getObjectId());
        // notify Kill-Quests
        int worldId = winner.getWorldId();
        QuestEngine.getInstance().onKillInWorld(new QuestEnv(victim, winner, 0, 0), worldId);
        QuestEngine.getInstance().onKillRanked(new QuestEnv(victim, winner, 0, 0), victim.getAbyssRank().getRank());
        CustomQuestsService.getInstance().onPlayerKill(winner, victim);
        return true;
    }

    private static boolean applyBoost(Player player){

        if(PvPConfig.SOLOBOOST_MAP_IDS == null || PvPConfig.SOLOBOOST_MAP_IDS.length == 0)
            return false;

        int size = PvPConfig.SOLOBOOST_MAP_IDS.length;

        for (int i = 0; i < size; i++){
            if(player.getWorldId() == PvPConfig.SOLOBOOST_MAP_IDS[i])
                return true;
        }

        return false;
    }
}
