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
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.siege.SiegeRace;

public class SiegeCounter {

    private final Map<SiegeRace, SiegeRaceCounter> siegeRaceCounters = Maps.newHashMap();

    public SiegeCounter() {
        siegeRaceCounters.put(SiegeRace.ELYOS, new SiegeRaceCounter(SiegeRace.ELYOS));
        siegeRaceCounters.put(SiegeRace.ASMODIANS, new SiegeRaceCounter(SiegeRace.ASMODIANS));
        siegeRaceCounters.put(SiegeRace.BALAUR, new SiegeRaceCounter(SiegeRace.BALAUR));
    }

    public void addDamage(Creature creature, int damage) {
        SiegeRace siegeRace;
        if (creature instanceof Player) {
            siegeRace = SiegeRace.getByRace(creature.getRace());
        } else if (creature instanceof SiegeNpc) {
            siegeRace = ((SiegeNpc) creature).getSiegeRace();
        } else {
            return;
        }

        siegeRaceCounters.get(siegeRace).addPoints(creature, damage);
    }

    public void addAbyssPoints(Player player, int ap) {
        SiegeRace sr = SiegeRace.getByRace(player.getRace());
        siegeRaceCounters.get(sr).addAbyssPoints(player, ap);
    }

    public SiegeRaceCounter getRaceCounter(SiegeRace race) {
        return siegeRaceCounters.get(race);
    }

    public void addRaceDamage(SiegeRace race, int damage) {
        getRaceCounter(race).addTotalDamage(damage);
    }

    /**
     * Returns list of siege race counters sorted by total damage done to siege boss. Sorted in descending order.
     *
     * @return all siege race damage counters sorted by descending order
     */
    public SiegeRaceCounter getWinnerRaceCounter() {
        List<SiegeRaceCounter> list = Lists.newArrayList(siegeRaceCounters.values());
        Collections.sort(list);
        return list.get(0);
    }

}
