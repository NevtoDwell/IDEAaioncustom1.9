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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.commons.utils.collections.Partitioner;
import com.ne.gs.configs.modules.AbyssRankConfig;
import com.ne.gs.database.dao.AbyssRankDAO;
import com.ne.gs.model.AbyssRankingResult;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.network.aion.serverpackets.SM_ABYSS_RANKING_LEGIONS;
import com.ne.gs.network.aion.serverpackets.SM_ABYSS_RANKING_PLAYERS;
import com.ne.gs.services.LegionService;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author VladimirZ
 */
public class AbyssRankingCache {

    private static final Logger log = LoggerFactory.getLogger(AbyssRankingCache.class);
    private int lastUpdate;
    private final Map<Race, List<SM_ABYSS_RANKING_PLAYERS>> players = new THashMap<>();
    private final Map<Race, SM_ABYSS_RANKING_LEGIONS> legions = new THashMap<>();

    private volatile Set<Integer> _offlinerUids = Collections.emptySet();

    public void onPlayerEnterWorld(Player player) {
        if (AbyssRankConfig.OFFLINERS && _offlinerUids.contains(player.getObjectId())) {
            player.sendMsg("Вы были исключены из рейтинга бездны из-за длительного отсутствия в игре. " +
                "После рестарта сервера участие в рейтинге возобновится.");
        }
    }

    public void reloadRankings() {
        log.info("Updating abyss ranking cache");
        lastUpdate = (int) (System.currentTimeMillis() / 1000);
        getDAO().updateRankList();

        renewPlayerRanking(Race.ASMODIANS);
        renewPlayerRanking(Race.ELYOS);

        renewLegionRanking();

        World.getInstance().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                player.resetAbyssRankListUpdated();
            }
        });
    }

    private void renewLegionRanking() {
        Map<Integer, Integer> newLegionRankingCache = new HashMap<>();
        ArrayList<AbyssRankingResult> elyosRanking = getDAO().getAbyssRankingLegions(Race.ELYOS);
        ArrayList<AbyssRankingResult> asmoRanking = getDAO().getAbyssRankingLegions(Race.ASMODIANS);

        legions.clear();
        legions.put(Race.ASMODIANS, new SM_ABYSS_RANKING_LEGIONS(lastUpdate, asmoRanking, Race.ASMODIANS));
        legions.put(Race.ELYOS, new SM_ABYSS_RANKING_LEGIONS(lastUpdate, elyosRanking, Race.ELYOS));

        for (AbyssRankingResult result : elyosRanking) {
            newLegionRankingCache.put(result.getLegionId(), result.getRankPos());
        }
        for (AbyssRankingResult result : asmoRanking) {
            newLegionRankingCache.put(result.getLegionId(), result.getRankPos());
        }
        LegionService.getInstance().performRankingUpdate(newLegionRankingCache);
    }

    private void renewPlayerRanking(Race race) {
        List<SM_ABYSS_RANKING_PLAYERS> newlyCalculated = generatePacketsForRace(race);
        players.remove(race);
        players.put(race, newlyCalculated);
    }

    private List<SM_ABYSS_RANKING_PLAYERS> generatePacketsForRace(final Race race) {
        // players orderd by ap
        List<AbyssRankingResult> ranks = new ArrayList<>();
        Set<Integer> offlinerUids = new THashSet<>();
        for (AbyssRankingResult rank : getDAO().getAbyssRankingPlayers(race)) {
            PlayerCommonData pcd = PlayerCommonData.get(rank.getPlayerId());
            if (pcd != null && AbyssRankConfig.OFFLINERS) {
                DateTime now = DateTime.now();
                DateTime lastOnline = new DateTime(pcd.getLastOnline());
                int daysOffline = Days.daysBetween(lastOnline, now).getDays();
                if (daysOffline >= AbyssRankConfig.OFFLINERS_DAYS) {
                    offlinerUids.add(rank.getPlayerId());
                    continue;
                }
            }

            ranks.add(rank);
        }

        _offlinerUids = ImmutableSet.copyOf(offlinerUids);

        final List<SM_ABYSS_RANKING_PLAYERS> playerPackets = new ArrayList<>();
        final MutableInt page = new MutableInt(1);
        final MutableInt rankPos = new MutableInt(1);
        Partitioner.of(ranks, 46).foreach(new Partitioner.Func2<AbyssRankingResult>() {
            @Override
            public boolean apply(List<AbyssRankingResult> list, boolean first, boolean last) {
                for (AbyssRankingResult r : list) {
                    r.setRankPos(rankPos.intValue());
                    rankPos.increment();
                }

                playerPackets.add(new SM_ABYSS_RANKING_PLAYERS(lastUpdate, list, race, page.intValue(), last));
                page.increment();
                return true;
            }
        });

        return playerPackets;
    }

    /**
     * @return all players
     */
    public List<SM_ABYSS_RANKING_PLAYERS> getPlayers(Race race) {
        return players.get(race);
    }

    /**
     * @return all legions
     */
    public SM_ABYSS_RANKING_LEGIONS getLegions(Race race) {
        return legions.get(race);
    }

    /**
     * @return the lastUpdate
     */
    public int getLastUpdate() {
        return lastUpdate;
    }

    private static AbyssRankDAO getDAO() {
        return GDB.get(AbyssRankDAO.class);
    }

    public static AbyssRankingCache getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {

        protected static final AbyssRankingCache INSTANCE = new AbyssRankingCache();
    }
}
