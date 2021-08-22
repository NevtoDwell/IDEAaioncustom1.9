/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.zone;

import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.zone.ZoneInfo;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author MrPoke
 */
public class SiegeZoneInstance extends ZoneInstance {

    private static final Logger log = LoggerFactory.getLogger(SiegeZoneInstance.class);

    private final FastMap<Integer, Player> players = new FastMap<>();

    /**
     * @param mapId
     * @param template
     */
    public SiegeZoneInstance(int mapId, ZoneInfo template) {
        super(mapId, template);
    }

    @Override
    public boolean onEnter(Creature creature) {
        if (super.onEnter(creature)) {
            if (creature instanceof Player) {
                players.put(creature.getObjectId(), (Player) creature);
            }
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean onLeave(Creature creature) {
        if (super.onLeave(creature)) {
            if (creature instanceof Player) {
                players.remove(creature.getObjectId());
            }
            return true;
        }
        return false;
    }

    public void doOnAllPlayers(Visitor<Player> visitor) {
        try {
            for (FastMap.Entry<Integer, Player> e = players.head(), mapEnd = players.tail(); (e = e.getNext()) != mapEnd; ) {
                Player player = e.getValue();
                if (player != null) {
                    visitor.visit(player);
                }
            }
        } catch (Exception ex) {
            log.error("Exception when running visitor on all players" + ex);
        }
    }
}
