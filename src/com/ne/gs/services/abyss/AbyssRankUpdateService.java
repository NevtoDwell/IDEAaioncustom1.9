/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.abyss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.commons.services.CronService;
import com.ne.gs.configs.main.RankingConfig;
import com.ne.gs.configs.modules.AbyssRankConfig;
import com.ne.gs.database.dao.AbyssRankDAO;
import com.ne.gs.database.dao.ServerVariablesDAO;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.AbyssRank;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.utils.stats.AbyssRankEnum;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author ATracer
 */
public class AbyssRankUpdateService {

    private static final Logger log = LoggerFactory.getLogger(AbyssRankUpdateService.class);

    private AbyssRankUpdateService() {
    }

    public static AbyssRankUpdateService getInstance() {
        return SingletonHolder.instance;
    }

    public void scheduleUpdate() {
        ServerVariablesDAO dao = GDB.get(ServerVariablesDAO.class);
        int nextTime = dao.load("abyssRankUpdate");
        if (nextTime < System.currentTimeMillis() / 1000) {
            performUpdate();
        }

        log.info("Starting ranking update task based on cron expression: " + RankingConfig.TOP_RANKING_UPDATE_RULE);
        CronService.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                performUpdate();
            }
        }, RankingConfig.TOP_RANKING_UPDATE_RULE, true);
    }

    /**
     * Perform update of all ranks
     */
    public void performUpdate() {
        log.info("AbyssRankUpdateService: executing rank update");
        long startTime = System.currentTimeMillis();

        World.getInstance().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                player.getAbyssRank().doUpdate();
                GDB.get(AbyssRankDAO.class).storeAbyssRank(player);
            }
        });

        updateLimitedRanks();
        AbyssRankingCache.getInstance().reloadRankings();
        log.info("AbyssRankUpdateService: execution time: " + (System.currentTimeMillis() - startTime) / 1000);
    }

    /**
     * Update player ranks based on quota for all players (online/offline)
     */
    private void updateLimitedRanks() {
        updateAllRanksForRace(Race.ASMODIANS, AbyssRankEnum.STAR1_OFFICER.getRequired());
        updateAllRanksForRace(Race.ELYOS, AbyssRankEnum.STAR1_OFFICER.getRequired());
    }

    private void updateAllRanksForRace(Race race, int apLimit) {
        Map<Integer, Integer> playerApMap = GDB.get(AbyssRankDAO.class).loadPlayersAp(race, apLimit);

        List<Entry<Integer, Integer>> playerApEntries =
            new ArrayList<>(
                Collections2.filter(playerApMap.entrySet(),
                    new Predicate<Entry<Integer, Integer>>() {
                        @Override
                        public boolean apply(Entry<Integer, Integer> e) {
                            PlayerCommonData pcd = PlayerCommonData.get(e.getKey());
                            if (pcd != null && AbyssRankConfig.OFFLINERS) {
                                DateTime now = DateTime.now();
                                DateTime lastOnline = new DateTime(pcd.getLastOnline());
                                int daysOffline = Days.daysBetween(lastOnline, now).getDays();
                                if (daysOffline >= AbyssRankConfig.OFFLINERS_DAYS) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    }
                )
            );

        Collections.sort(playerApEntries, PLAYER_AP_COMPARATOR);

        selectRank(AbyssRankEnum.SUPREME_COMMANDER, playerApEntries);
        selectRank(AbyssRankEnum.COMMANDER, playerApEntries);
        selectRank(AbyssRankEnum.GREAT_GENERAL, playerApEntries);
        selectRank(AbyssRankEnum.GENERAL, playerApEntries);
        selectRank(AbyssRankEnum.STAR5_OFFICER, playerApEntries);
        selectRank(AbyssRankEnum.STAR4_OFFICER, playerApEntries);
        selectRank(AbyssRankEnum.STAR3_OFFICER, playerApEntries);
        selectRank(AbyssRankEnum.STAR2_OFFICER, playerApEntries);
        selectRank(AbyssRankEnum.STAR1_OFFICER, playerApEntries);
        updateToNoQuotaRank(playerApEntries);
    }

    private void selectRank(AbyssRankEnum rank, List<Entry<Integer, Integer>> playerApEntries) {
        int quota = rank.getId() < 18 ? rank.getQuota() - AbyssRankEnum.getRankById(rank.getId() + 1)
            .getQuota() : rank.getQuota();
        for (int i = 0; i < quota; i++) {
            if (playerApEntries.isEmpty()) {
                return;
            }
            // check next player in list
            Entry<Integer, Integer> playerAp = playerApEntries.get(0);
            // check if there are some players left in map
            if (playerAp == null) {
                return;
            }
            int playerId = playerAp.getKey();
            int ap = playerAp.getValue();
            // check if this (and the rest) player has required ap count
            if (ap < rank.getRequired()) {
                return;
            }
            // remove player and update its rank
            playerApEntries.remove(0);
            updateRankTo(rank, playerId);
        }
    }

    private void updateToNoQuotaRank(List<Entry<Integer, Integer>> playerApEntries) {
        for (Entry<Integer, Integer> playerApEntry : playerApEntries) {
            updateRankTo(AbyssRankEnum.GRADE1_SOLDIER, playerApEntry.getKey());
        }
    }

    protected void updateRankTo(AbyssRankEnum newRank, int playerId) {
        // check if rank is changed for online players
        Player onlinePlayer = World.getInstance().findPlayer(playerId);
        if (onlinePlayer != null) {
            AbyssRank abyssRank = onlinePlayer.getAbyssRank();
            AbyssRankEnum currentRank = abyssRank.getRank();
            if (currentRank != newRank) {
                abyssRank.setRank(newRank);
                AbyssPointsService.checkRankChanged(onlinePlayer, currentRank, newRank);
            }
        } else {
            GDB.get(AbyssRankDAO.class).updateAbyssRank(playerId, newRank);
        }
    }

    private static final class SingletonHolder {

        protected static final AbyssRankUpdateService instance = new AbyssRankUpdateService();
    }

    private static final Comparator<Entry<Integer, Integer>> PLAYER_AP_COMPARATOR = new Comparator<Entry<Integer, Integer>>() {
        @Override
        public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
            return -o1.getValue().compareTo(o2.getValue()); // descending order
        }
    };

}
