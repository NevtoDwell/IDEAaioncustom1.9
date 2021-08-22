/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javolution.util.FastMap;

import com.ne.commons.utils.GenericValidator;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.world.World;

/**
 * A class that contains all the counters for the siege. One SiegeCounter per race should be used.
 *
 * @author SoulKeeper
 */
public class SiegeRaceCounter implements Comparable<SiegeRaceCounter> {

    private final AtomicLong totalDamage = new AtomicLong();

    private final Map<Integer, AtomicLong> playerDamageCounter = new FastMap<Integer, AtomicLong>().shared();

    private final Map<Integer, AtomicLong> playerAPCounter = new FastMap<Integer, AtomicLong>().shared();

    private final SiegeRace siegeRace;

    public SiegeRaceCounter(SiegeRace siegeRace) {
        this.siegeRace = siegeRace;
    }

    public void addPoints(Creature creature, int damage) {
        addTotalDamage(damage);

        if (creature instanceof Player) {
            addPlayerDamage((Player) creature, damage);
        }
    }

    public void addTotalDamage(int damage) {
        totalDamage.addAndGet(damage);
    }

    public void addPlayerDamage(Player player, int damage) {
        addToCounter(player.getObjectId(), damage, playerDamageCounter);
    }

    public void addAbyssPoints(Player player, int abyssPoints) {
        addToCounter(player.getObjectId(), abyssPoints, playerAPCounter);
    }

    protected <K> void addToCounter(K key, int value, Map<K, AtomicLong> counterMap) {
        // Get the counter for specific key
        AtomicLong counter = counterMap.get(key);

        // Counter was not registered, need to create it
        if (counter == null) {
            // synchronize here, it may happen that there will be attempt to increment
            // same counter from different threads
            synchronized (this) {
                if (counterMap.containsKey(key)) {
                    counter = counterMap.get(key);
                } else {
                    counter = new AtomicLong();
                    counterMap.put(key, counter);
                }
            }
        }

        counter.addAndGet(value);
    }

    public long getTotalDamage() {
        return totalDamage.get();
    }

    /**
     * Returns "player to damage" map. Map is ordered by damage in "descending" order
     *
     * @return map with legion damages
     */
    public Map<Integer, Long> getPlayerDamageCounter() {
        return getOrderedCounterMap(playerDamageCounter);
    }

    /**
     * Returns "player to abyss points" map. Map is ordered by abyssPoints in descending order
     *
     * @return map with player abyss points
     */
    public Map<Integer, Long> getPlayerAbyssPoints() {
        return getOrderedCounterMap(playerAPCounter);
    }

    protected <K> Map<K, Long> getOrderedCounterMap(Map<K, AtomicLong> unorderedMap) {
        if (GenericValidator.isBlankOrNull(unorderedMap)) {
            return Collections.emptyMap();
        }

        List<Map.Entry<K, AtomicLong>> tempList = Lists.newArrayList(unorderedMap.entrySet());
        Collections.sort(tempList, new Comparator<Map.Entry<K, AtomicLong>>() {
            @Override
            public int compare(Map.Entry<K, AtomicLong> o1, Map.Entry<K, AtomicLong> o2) {
                return new Long(o2.getValue().get()).compareTo(o1.getValue().get());
            }
        });

        Map<K, Long> result = Maps.newLinkedHashMap();
        for (Map.Entry<K, AtomicLong> entry : tempList) {
            if (entry.getValue().get() > 0) {
                result.put(entry.getKey(), entry.getValue().get());
            }
        }
        return result;
    }

    @Override
    public int compareTo(SiegeRaceCounter o) {
        return new Long(o.getTotalDamage()).compareTo(getTotalDamage());
    }

    public SiegeRace getSiegeRace() {
        return siegeRace;
    }

    /**
     * Returns Legion id if damage done by that legion is > than 50% of total damage
     *
     * @return legion id or null if none
     */
    public Integer getWinnerLegionId() {
        Map<Player, AtomicLong> teamDamageMap = new HashMap<>();
        for (Integer id : playerDamageCounter.keySet()) {
            Player player = World.getInstance().findPlayer(id);

            if (player != null && player.getCurrentTeam() != null) {
                Player teamLeader = player.getCurrentTeam().getLeaderObject();
                long damage = playerDamageCounter.get(id).get();
                if (teamLeader != null) {
                    if (!teamDamageMap.containsKey(teamLeader)) {
                        teamDamageMap.put(teamLeader, new AtomicLong());
                    }
                    teamDamageMap.get(teamLeader).addAndGet(damage);
                }
            }
        }

        if (teamDamageMap.isEmpty()) {
            return null;
        }

        Player topTeamLeader = getOrderedCounterMap(teamDamageMap).keySet().iterator().next();
        Legion legion = topTeamLeader.getLegion();

        return legion != null ? legion.getLegionId() : null;
    }
}
