/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager;

import com.mw.GlobalConst;
import com.ne.commons.configs.CommonsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.AEFastSet;
import com.ne.commons.utils.concurrent.RunnableStatsManager;

/**
 * @author lord_rex and MrPoke based on l2j-free engines.
 */
public abstract class AbstractFIFOPeriodicTaskManager<T> extends AbstractPeriodicTaskManager {

    protected static final Logger log = LoggerFactory.getLogger(AbstractFIFOPeriodicTaskManager.class);

    private final AEFastSet<T> queue = new AEFastSet<>();

    private final AEFastSet<T> activeTasks = new AEFastSet<>();

    public AbstractFIFOPeriodicTaskManager(int period) {
        super(period);
    }

    public final void add(T t) {
        writeLock();
        try {
            queue.add(t);
        } finally {
            writeUnlock();
        }
    }

    @Override
    public final void run() {
        writeLock();
        try {
            activeTasks.addAll(queue);

            queue.clear();
        } finally {
            writeUnlock();
        }

        for (T task; (task = activeTasks.removeFirst()) != null; ) {
            long begin = System.nanoTime();

            try {
                callTask(task);
            } catch (RuntimeException e) {
                log.warn("", e);
            } finally {

                if (GlobalConst.EnableMethodStatsLog)
                    RunnableStatsManager.handleStats(task.getClass(), getCalledMethodName(), System.nanoTime() - begin);
            }
        }
    }

    protected abstract void callTask(T task);

    protected abstract String getCalledMethodName();
}
