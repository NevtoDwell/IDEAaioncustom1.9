/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.ne.commons.services.CronService;
import com.ne.gs.database.GDB;
import com.ne.gs.taskmanager.tasks.SpawnTask;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.dao.TaskFromDBDAO;
import com.ne.gs.model.tasks.TaskFromDB;
import com.ne.gs.model.templates.tasks.TaskFromDBHandler;
import com.ne.gs.taskmanager.tasks.RestartTask;
import com.ne.gs.taskmanager.tasks.ShutdownTask;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author Divinity Based on L2J Emulator Global Tasks System
 * @author From L2J : Layane
 */
public class TaskManagerFromDB {

    private static final Logger log = LoggerFactory.getLogger(TaskManagerFromDB.class);

    private final ArrayList<TaskFromDB> tasksList;
    private final HashMap<String, TaskFromDBHandler> handlers;

    public TaskManagerFromDB() {
        handlers = new HashMap<>();

        tasksList = getDAO().getAllTasks();
        log.info("Loaded " + tasksList.size() + " task" + (tasksList.size() > 1 ? "s" : "") + " from the database");

        registerHandlers();
        registerTasks();
    }

    /**
     * Allow to register all tasks to the handler
     */
    private void registerHandlers() {
        registerNewTask(new ShutdownTask());
        registerNewTask(new RestartTask());
        registerNewTask(new SpawnTask());
    }

    /**
     * Allow to register one task and check if already exists
     *
     * @param task
     */
    private void registerNewTask(TaskFromDBHandler task) {
        if (handlers.get(task.getTaskName()) != null) {
            log.error("Can't override a task with name : " + task.getTaskName());
        }

        handlers.put(task.getTaskName(), task);
    }

    /**
     * Launching & checking task process
     */
    private void registerTasks() {
        // For all tasks from GDB
        for (TaskFromDB task : tasksList) {
            // If the task name exist
            if (handlers.get(task.getName()) != null) {
                Class<? extends TaskFromDBHandler> tmpClass = handlers.get(task.getName()).getClass();
                TaskFromDBHandler currentTask = null;

                try {
                    // Create new instance of the task
                    currentTask = tmpClass.newInstance();
                } catch (InstantiationException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }

                // Set informations for the task
                currentTask.setId(task.getId());
                currentTask.setParam(task.getParams());

                if (!currentTask.isValid()) {
                    log.error("Invalid parameter for task ID: " + task.getId());
                    continue;
                }

                if (task.getType().equals("FIXED_IN_TIME")) {
                    runFixedInTimeTask(currentTask, task);
                } else if (task.getType().equals("WITH_CRON")) {
                    runWithCronTask(currentTask, task);
                } else {
                    log.error("Unknow task's type for " + task.getType());
                }
            } else {
                log.error("Unknow task's name with ID : " + task.getName());
            }
        }
    }

    /**
     * Run a fixed in the time (HH:MM:SS) task
     *
     * @param handler
     * @param dbTask
     */
    private void runFixedInTimeTask(TaskFromDBHandler handler, TaskFromDB dbTask) {
        String time[] = dbTask.getStartTime().split(":");
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);
        int second = Integer.parseInt(time[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        if (delay < 0) {
            delay += 1 * 24 * 60 * 60 * 1000;
        }

        ThreadPoolManager.getInstance().scheduleAtFixedRate(handler, delay, 1 * 24 * 60 * 60 * 1000);
    }

    /**
     * Run task with CronExpression.
     * @param handler
     * @param dbTask
     */
    private void runWithCronTask(TaskFromDBHandler handler, TaskFromDB dbTask) {
        String cron = dbTask.getStartTime();

        if (!CronExpression.isValidExpression(cron)) {
            log.error("Task[" + dbTask.getId() + "] Start time is not valid cron expression!");
            return;
        }
        try {
            CronService.getInstance().schedule(handler, cron);
        } catch (Exception e) {
            log.warn("Task[" + dbTask.getId() + "] Can't start task with cron. My be start_date in past. Start through 2 second.");
        }
        ThreadPoolManager.getInstance().schedule(handler, 2000L);
    }

    /**
     * Retuns {@link com.ne.gs.database.dao.TaskFromDBDAO} , just a shortcut
     *
     * @return {@link com.ne.gs.database.dao.TaskFromDBDAO}
     */
    private static TaskFromDBDAO getDAO() {
        return GDB.get(TaskFromDBDAO.class);
    }

    /**
     * Get the instance
     *
     * @return
     */
    public static TaskManagerFromDB getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * SingletonHolder
     */
    private static final class SingletonHolder {

        protected static final TaskManagerFromDB instance = new TaskManagerFromDB();
    }
}
