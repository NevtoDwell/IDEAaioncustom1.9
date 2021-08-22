/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.spawnengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ch.lambdaj.group.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.walker.WalkerTemplate;

import static ch.lambdaj.Lambda.*;

/**
 * Forms the walker groups on initial spawn<br>
 * Brings NPCs back to their positions if they die<br>
 * Cleanup and rework will be made after tests and error handling<br>
 * To use only with patch!
 *
 * @author vlog
 * @based on Imaginary's imagination
 * @modified Rolandas
 */
public class WalkerFormator {

    private static final Logger log = LoggerFactory.getLogger(WalkerFormator.class);
    private final Map<String, List<ClusteredNpc>> groupedSpawnObjects;
    private final Map<String, WalkerGroup> walkFormations = new HashMap<>();

    public boolean processClusteredNpc(Npc npc, int instance) {
        SpawnTemplate spawn = npc.getSpawn();
        if (spawn.getWalkerId() != null) {
            if (walkFormations.containsKey(spawn.getWalkerId())) {
                WalkerGroup wg = walkFormations.get(spawn.getWalkerId());
                npc.setWalkerGroup(wg);
                wg.respawn(npc);
                return false;
            }
            WalkerTemplate template = DataManager.WALKER_DATA.getWalkerTemplate(spawn.getWalkerId());
            if (template == null) {
                log.warn("Missing walker ID: " + spawn.getWalkerId());
                return false;
            }
            if (template.getPool() < 2) {
                return false;
            }
            ClusteredNpc candidate = new ClusteredNpc(npc, instance, template);
            List<ClusteredNpc> candidateList = null;
            if (groupedSpawnObjects.containsKey(spawn.getWalkerId())) {
                candidateList = groupedSpawnObjects.get(spawn.getWalkerId());
            } else {
                candidateList = new ArrayList<>();
                groupedSpawnObjects.put(spawn.getWalkerId(), candidateList);
            }
            return candidateList.add(candidate);
        }
        return false;
    }

    public void organizeAndSpawn() {
        try {
            for (List<ClusteredNpc> candidates : groupedSpawnObjects.values()) {
                Group<ClusteredNpc> bySize = group(candidates, by(on(ClusteredNpc.class).getPositionHash()));
                Set<String> keys = bySize.keySet();
                int maxSize = 0;
                List<ClusteredNpc> npcs = null;
                for (String key : keys) {
                    if (bySize.find(key).size() > maxSize) {
                        npcs = bySize.find(key);
                        maxSize = npcs.size();
                    }
                }
                if (maxSize == 1) {
                    for (ClusteredNpc snpc : candidates) {
                        snpc.spawn(snpc.getNpc().getZ());
                    }
                } else {
                    WalkerGroup wg = new WalkerGroup(npcs);
                    if (candidates.get(0).getWalkTemplate().getPool() != candidates.size()) {
                        log.warn("Incorrect pool for route: " + candidates.get(0).getWalkTemplate().getRouteId());
                    }
                    wg.form();
                    wg.spawn();
                    walkFormations.put(candidates.get(0).getWalkTemplate().getRouteId(), wg);
                    for (ClusteredNpc snpc : candidates) {
                        if (npcs.contains(snpc)) {
                            continue;
                        }
                        snpc.spawn(snpc.getNpc().getZ());
                    }
                }
            }
            clear();
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    private void clear() {
        groupedSpawnObjects.clear();
    }

    private WalkerFormator() {
        groupedSpawnObjects = new HashMap<>();
    }

    public static WalkerFormator getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder {

        protected static final WalkerFormator instance = new WalkerFormator();
    }
}
