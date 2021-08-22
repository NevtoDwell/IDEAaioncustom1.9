/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.taskmanager.AbstractLockManager;
import com.ne.commons.utils.Rnd;
import com.ne.gs.GameServer;
import com.ne.gs.GameServer.StartupHook;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author lord_rex and MrPoke based on l2j-free engines. This can be used for periodic calls.
 */
public abstract class AbstractPeriodicTaskManager extends AbstractLockManager implements Runnable, StartupHook {

    protected static final Logger log = LoggerFactory.getLogger(AbstractPeriodicTaskManager.class);

    private final int period;

    public AbstractPeriodicTaskManager(int period) {
        this.period = period;

        GameServer.addStartupHook(this);

        log.info(getClass().getSimpleName() + ": Initialized.");
    }

    @Override
    public final void onStartup() {
        ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000 + Rnd.get(period), Rnd.get(period - 5, period + 5));
    }

    @Override
    public abstract void run();
}
