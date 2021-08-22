/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.EventsConfig;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author Source
 */
public final class PlayerEventService {

    private static final Logger log = LoggerFactory.getLogger(PlayerEventService.class);

    private PlayerEventService() {

        final EventCollector visitor = new EventCollector();
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                World.getInstance().doOnAllPlayers(visitor);
            }

        }, EventsConfig.EVENT_PERIOD * 60000, EventsConfig.EVENT_PERIOD * 60000);
    }

    private static final class EventCollector implements Visitor<Player> {

        @Override
        public void visit(Player player) {
            int membership = player.getClientConnection().getAccount().getMembership();
            int rate = EventsConfig.EVENT_REWARD_MEMBERSHIP_RATE ? membership + 1 : 1;
            if (membership >= EventsConfig.EVENT_REWARD_MEMBERSHIP) {
                try {
                    if (player.getInventory().isFull()) {
                        log.warn("[EventReward] player " + player.getName() + " tried to receive item with full inventory.");
                    } else {
                        ItemService.addItem(player, player.getRace() == Race.ELYOS ? EventsConfig.EVENT_ITEM_ELYOS : EventsConfig.EVENT_ITEM_ASMO,
                            EventsConfig.EVENT_ITEM_COUNT * rate);
                    }
                } catch (Exception ex) {
                    log.error("Exception during event rewarding of player " + player.getName(), ex);
                }
            }
        }

    }

    ;

    public static PlayerEventService getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder {

        protected static final PlayerEventService instance = new PlayerEventService();
    }

}
