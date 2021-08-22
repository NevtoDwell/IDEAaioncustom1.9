/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.attack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javolution.util.FastMap;

import com.ne.gs.configs.main.PvPConfig;

/**
 * @author Sarynth
 */
public class KillList {

    private final FastMap<Integer, List<Long>> killList;

    public KillList() {
        killList = new FastMap<>();
    }

    public int getFrequentKillsFor(int victimId) {
        return getKillsFor(victimId, PvPConfig.CHAIN_KILL_TIME_RESTRICTION);
    }

    public int getDailyKillsFor(int victimId) {
        return getKillsFor(victimId, TimeUnit.DAYS.toMillis(1));
    }

    public int getQuestFrequentKillsFor(int victimId) {
        return getKillsFor(victimId, PvPConfig.QUEST_CHAIN_KILL_TIME_RESTRICTION);
    }

    public synchronized int getKillsFor(int victimId, long timeRestriction) {
        List<Long> killTimes = killList.get(victimId);

        if (killTimes == null) {
            return 0;
        }

        long now = System.currentTimeMillis();

        int killCount = 0;
        for (Iterator<Long> i = killTimes.iterator(); i.hasNext(); ) {
            if (now - i.next() > timeRestriction) {
                i.remove();
            } else {
                killCount++;
            }
        }

        return killCount;
    }

    public synchronized void addKillFor(int victimId) {
        List<Long> killTimes = killList.get(victimId);
        if (killTimes == null) {
            killTimes = new ArrayList<>();
            killList.put(victimId, killTimes);
        }

        killTimes.add(System.currentTimeMillis());
    }

}
