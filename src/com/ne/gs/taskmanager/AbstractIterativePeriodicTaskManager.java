/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager;

import java.util.Set;

import com.mw.GlobalConst;
import com.ne.commons.configs.CommonsConfig;
import javolution.util.FastSet;

import com.ne.commons.utils.concurrent.RunnableStatsManager;

/**
 * @author NB4L1
 */
public abstract class AbstractIterativePeriodicTaskManager<T> extends AbstractPeriodicTaskManager {

    private final Set<T> startList = new FastSet<>();
    private final Set<T> stopList = new FastSet<>();

    private final FastSet<T> activeTasks = new FastSet<>();

    protected AbstractIterativePeriodicTaskManager(int period) {
        super(period);
    }

    public boolean hasTask(T task) {
        readLock();
        try {
            if (stopList.contains(task)) {
                return false;
            }

            return activeTasks.contains(task) || startList.contains(task);
        } finally {
            readUnlock();
        }
    }

    public void startTask(T task) {
        writeLock();
        try {
            startList.add(task);

            stopList.remove(task);
        } finally {
            writeUnlock();
        }
    }

    public void stopTask(T task) {
        writeLock();
        try {
            stopList.add(task);

            startList.remove(task);
        } finally {
            writeUnlock();
        }
    }

    @Override
    public final void run() {
        writeLock();
        try {
            activeTasks.addAll(startList);
            activeTasks.removeAll(stopList);

            startList.clear();
            stopList.clear();
        } finally {
            writeUnlock();
        }

        for (FastSet.Record r = activeTasks.head(), end = activeTasks.tail(); (r = r.getNext()) != end; ) {
            T task = activeTasks.valueOf(r);
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
