/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ne.commons.utils.chmv8.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.concurrent.AionRejectedExecutionHandler;
import com.ne.commons.utils.concurrent.RunnableWrapper;
import com.ne.gs.configs.main.ThreadConfig;

/**
 * @author -Nemesiss-, NB4L1, MrPoke, lord_rex
 */
public final class ThreadPoolManager {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolManager.class);

    public static final long MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING = 5000;
    private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;

    private final ScheduledThreadPoolExecutor scheduledPool;
    private final ThreadPoolExecutor instantPool;
    private final ThreadPoolExecutor longRunningPool;
    private final ForkJoinPool workStealingPool;

    private ThreadPoolManager() {
        int instantPoolSize = Math.max(1, ThreadConfig.THREAD_POOL_SIZE / 3);

        scheduledPool = new ScheduledThreadPoolExecutor(ThreadConfig.THREAD_POOL_SIZE - instantPoolSize);
        scheduledPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
        scheduledPool.prestartAllCoreThreads();

        instantPool = new ThreadPoolExecutor(instantPoolSize, instantPoolSize, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100000));
        instantPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
        instantPool.prestartAllCoreThreads();

        longRunningPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        longRunningPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
        longRunningPool.prestartAllCoreThreads();

        WorkStealThreadFactory forkJoinThreadFactory = new WorkStealThreadFactory("ForkJoinPool");
        workStealingPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2, forkJoinThreadFactory,
                new ThreadUncaughtExceptionHandler(), true);
        forkJoinThreadFactory.setDefaultPool(workStealingPool);

        scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                purge();
            }
        }, 150000, 150000);

        log.info("ThreadPoolManager: Initialized with " + scheduledPool.getPoolSize() + " scheduler, " + instantPool.getPoolSize() + " instant, "
            + longRunningPool.getPoolSize() + " long running thread(s).");
    }

    private long validate(long delay) {
        return Math.max(0, Math.min(MAX_DELAY, delay));
    }

    private static final class ThreadPoolRunnableWrapper extends RunnableWrapper {

        private ThreadPoolRunnableWrapper(Runnable runnable) {
            super(runnable, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING);
        }
    }

    public final ScheduledFuture<?> schedule(Runnable r, long delay) {
        r = new ThreadPoolRunnableWrapper(r);
        delay = validate(delay);
        return scheduledPool.schedule(r, delay, TimeUnit.MILLISECONDS);
    }

    public final ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period) {
        r = new ThreadPoolRunnableWrapper(r);
        delay = validate(delay);
        period = validate(period);
        return scheduledPool.scheduleAtFixedRate(r, delay, period, TimeUnit.MILLISECONDS);
    }

    public final void execute(Runnable r) {
        r = new ThreadPoolRunnableWrapper(r);
        instantPool.execute(r);
    }

    public final void executeLongRunning(Runnable r) {
        r = new RunnableWrapper(r);

        longRunningPool.execute(r);
    }

    public final Future<?> submit(Runnable r) {
        r = new ThreadPoolRunnableWrapper(r);

        return instantPool.submit(r);
    }

    public final Future<?> submitLongRunning(Runnable r) {
        r = new RunnableWrapper(r);

        return longRunningPool.submit(r);
    }

    public ForkJoinPool getForkingPool() {
        return workStealingPool;
    }

    /**
     * Executes a loginServer packet task
     *
     * @param pkt
     *     runnable packet for Login Server
     */
    public void executeLsPacket(Runnable pkt) {
        execute(pkt);
    }

    public void purge() {
        scheduledPool.purge();
        instantPool.purge();
        longRunningPool.purge();
    }

    /**
     * Shutdown all thread pools.
     */
    public void shutdown() {
        long begin = System.currentTimeMillis();

        log.info("ThreadPoolManager: Shutting down.");
        log.info("\t... executing " + getTaskCount(scheduledPool) + " scheduled tasks.");
        log.info("\t... executing " + getTaskCount(instantPool) + " instant tasks.");
        log.info("\t... executing " + getTaskCount(longRunningPool) + " long running tasks.");

        scheduledPool.shutdown();
        instantPool.shutdown();
        longRunningPool.shutdown();

        boolean success = false;
        try {
            success |= awaitTermination(5000);

            scheduledPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            scheduledPool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

            success |= awaitTermination(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("\t... success: " + success + " in " + (System.currentTimeMillis() - begin) + " msec.");
        log.info("\t... " + getTaskCount(scheduledPool) + " scheduled tasks left.");
        log.info("\t... " + getTaskCount(instantPool) + " instant tasks left.");
        log.info("\t... " + getTaskCount(longRunningPool) + " long running tasks left.");
    }

    private int getTaskCount(ThreadPoolExecutor tp) {
        return tp.getQueue().size() + tp.getActiveCount();
    }

    public List<String> getStats() {
        List<String> list = new ArrayList<>();

        list.add("");
        list.add("Scheduled pool:");
        list.add("=================================================");
        list.add("\tgetActiveCount: ...... " + scheduledPool.getActiveCount());
        list.add("\tgetCorePoolSize: ..... " + scheduledPool.getCorePoolSize());
        list.add("\tgetPoolSize: ......... " + scheduledPool.getPoolSize());
        list.add("\tgetLargestPoolSize: .. " + scheduledPool.getLargestPoolSize());
        list.add("\tgetMaximumPoolSize: .. " + scheduledPool.getMaximumPoolSize());
        list.add("\tgetCompletedTaskCount: " + scheduledPool.getCompletedTaskCount());
        list.add("\tgetQueuedTaskCount: .. " + scheduledPool.getQueue().size());
        list.add("\tgetTaskCount: ........ " + scheduledPool.getTaskCount());
        list.add("");
        list.add("Instant pool:");
        list.add("=================================================");
        list.add("\tgetActiveCount: ...... " + instantPool.getActiveCount());
        list.add("\tgetCorePoolSize: ..... " + instantPool.getCorePoolSize());
        list.add("\tgetPoolSize: ......... " + instantPool.getPoolSize());
        list.add("\tgetLargestPoolSize: .. " + instantPool.getLargestPoolSize());
        list.add("\tgetMaximumPoolSize: .. " + instantPool.getMaximumPoolSize());
        list.add("\tgetCompletedTaskCount: " + instantPool.getCompletedTaskCount());
        list.add("\tgetQueuedTaskCount: .. " + instantPool.getQueue().size());
        list.add("\tgetTaskCount: ........ " + instantPool.getTaskCount());
        list.add("");
        list.add("Long running pool:");
        list.add("=================================================");
        list.add("\tgetActiveCount: ...... " + longRunningPool.getActiveCount());
        list.add("\tgetCorePoolSize: ..... " + longRunningPool.getCorePoolSize());
        list.add("\tgetPoolSize: ......... " + longRunningPool.getPoolSize());
        list.add("\tgetLargestPoolSize: .. " + longRunningPool.getLargestPoolSize());
        list.add("\tgetMaximumPoolSize: .. " + longRunningPool.getMaximumPoolSize());
        list.add("\tgetCompletedTaskCount: " + longRunningPool.getCompletedTaskCount());
        list.add("\tgetQueuedTaskCount: .. " + longRunningPool.getQueue().size());
        list.add("\tgetTaskCount: ........ " + longRunningPool.getTaskCount());
        list.add("");

        return list;
    }

    private boolean awaitTermination(long timeoutInMillisec) throws InterruptedException {
        long begin = System.currentTimeMillis();

        while (System.currentTimeMillis() - begin < timeoutInMillisec) {
            if (!scheduledPool.awaitTermination(10, TimeUnit.MILLISECONDS) && scheduledPool.getActiveCount() > 0) {
                continue;
            }

            if (!instantPool.awaitTermination(10, TimeUnit.MILLISECONDS) && instantPool.getActiveCount() > 0) {
                continue;
            }

            if (!longRunningPool.awaitTermination(10, TimeUnit.MILLISECONDS) && longRunningPool.getActiveCount() > 0) {
                continue;
            }

            return true;
        }

        return false;
    }

    private static final class SingletonHolder {

        private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    }

    public static ThreadPoolManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
