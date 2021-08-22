/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.Iterator;
import java.util.concurrent.Future;
import com.google.common.collect.ImmutableList;
import com.ne.gs.database.GDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.PeriodicSaveConfig;
import com.ne.gs.database.dao.InventoryDAO;
import com.ne.gs.database.dao.ItemStoneListDAO;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class PeriodicSaveService {

    private static final Logger log = LoggerFactory.getLogger(PeriodicSaveService.class);

    private final Future<?> legionWhUpdateTask;

    public static PeriodicSaveService getInstance() {
        return SingletonHolder.instance;
    }

    private PeriodicSaveService() {

        int DELAY_LEGION_ITEM = PeriodicSaveConfig.LEGION_ITEMS * 1000;

        legionWhUpdateTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new LegionWhUpdateTask(), DELAY_LEGION_ITEM, DELAY_LEGION_ITEM);
    }

    private class LegionWhUpdateTask implements Runnable {

        @Override
        public void run() {
            //log.info("Legion WH update task started.");
            long startTime = System.currentTimeMillis();
            Iterator<Legion> legionsIterator = LegionService.getInstance().getCachedLegionIterator();
            int legionWhUpdated = 0;
            while (legionsIterator.hasNext()) {
                Legion legion = legionsIterator.next();
                ImmutableList<Item> allItems =
                        ImmutableList.<Item>builder()
                                .addAll(legion.getLegionWarehouse().getItemsWithKinah())
                                .addAll(legion.getLegionWarehouse().getDeletedItems()).build();

                try {
                    /**
                     * 1. save items first
                     */
                    GDB.get(InventoryDAO.class).store(allItems, null, null, legion.getLegionId());

                    /**
                     * 2. save item stones
                     */
                    GDB.get(ItemStoneListDAO.class).save(allItems);
                } catch (Exception ex) {
                    log.error("Exception during periodic saving of legion WH", ex);
                }

                legionWhUpdated++;
            }
            long workTime = System.currentTimeMillis() - startTime;
          //  log.info("Legion WH update: " + workTime + " ms, legions: " + legionWhUpdated + ".");
        }
    }

    /**
     * Save data on shutdown
     */
    public void onShutdown() {
        log.info("Starting data save on shutdown.");
        // save legion warehouse
        legionWhUpdateTask.cancel(false);
        new LegionWhUpdateTask().run();
        log.info("Data successfully saved.");
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final PeriodicSaveService instance = new PeriodicSaveService();
    }
}
