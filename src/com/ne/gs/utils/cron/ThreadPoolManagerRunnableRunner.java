/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.cron;

import com.ne.commons.services.cron.RunnableRunner;
import com.ne.gs.utils.ThreadPoolManager;

public class ThreadPoolManagerRunnableRunner extends RunnableRunner {

    @Override
    public void executeRunnable(Runnable r) {
        ThreadPoolManager.getInstance().execute(r);
    }

    @Override
    public void executeLongRunningRunnable(Runnable r) {
        ThreadPoolManager.getInstance().executeLongRunning(r);
    }
}
