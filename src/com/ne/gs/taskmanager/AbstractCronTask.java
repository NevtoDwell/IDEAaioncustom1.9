/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager;

import java.text.ParseException;
import java.util.Date;
import org.quartz.CronExpression;

import com.ne.gs.database.GDB;
import com.ne.commons.services.CronService;
import com.ne.gs.database.dao.ServerVariablesDAO;
import com.ne.gs.utils.ThreadPoolManager;

public abstract class AbstractCronTask implements Runnable {

    private final String cronExpressionString;
    private final CronExpression runExpression;
    private int runTime;
    private final long period;

    public final int getRunTime() {
        return runTime;
    }

    protected abstract long getRunDelay();

    protected void preInit() {
    }

    protected void postInit() {
    }

    public final String getCronExpressionString() {
        return cronExpressionString;
    }

    protected abstract String getServerTimeVariable();

    protected void preRun() {
    }

    protected abstract void executeTask();

    protected void postRun() {
    }

    public AbstractCronTask(String cronExpression) throws ParseException {
        if (cronExpression == null) {
            throw new NullPointerException("cronExpressionString");
        }
        cronExpressionString = cronExpression;

        ServerVariablesDAO dao = GDB.get(ServerVariablesDAO.class);
        runTime = dao.load(getServerTimeVariable());

        preInit();

        runExpression = new CronExpression(cronExpressionString);

        Date nextDate = runExpression.getTimeAfter(new Date());
        runTime = (int) (nextDate.getTime() / 1000L);
        Date nextAfterDate = runExpression.getTimeAfter(nextDate);
        period = nextAfterDate.getTime() - nextDate.getTime();
        postInit();

        if (getRunDelay() == 0L) {
            ThreadPoolManager.getInstance().schedule(this, 0L);
        }
        scheduleNextRun();
    }

    private void scheduleNextRun() {
        CronService.getInstance().schedule(this, cronExpressionString, true);
    }

    public long getPeriod() {
        return period;
    }

    @Override
    public final void run() {
        preRun();
        executeTask();

        Date nextDate = runExpression.getTimeAfter(new Date());
        ServerVariablesDAO dao = GDB.get(ServerVariablesDAO.class);
        runTime = (int) (nextDate.getTime() / 1000L);
        dao.store(getServerTimeVariable(), runTime);

        postRun();
    }
}
