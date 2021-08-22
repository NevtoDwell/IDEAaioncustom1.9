/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.tasks;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.TaskFromDBDAO;

/**
 * @author Divinity
 */
public abstract class TaskFromDBHandler implements Runnable {

    protected int id;
    protected String params[];

    /**
     * Task's id
     *
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Task's param(s)
     *
     * @param params
     *     String[]
     */
    public void setParam(String params[]) {
        this.params = params;
    }

    /**
     * The task's name This allow to check with the table column "task"
     */
    public abstract String getTaskName();

    /**
     * Check if the task's parameters are valid
     *
     * @return true if valid, false otherwise
     */
    public abstract boolean isValid();

    /**
     * Retuns {@link com.ne.gs.database.dao.TaskFromDBDAO} , just a shortcut
     *
     * @return {@link com.ne.gs.database.dao.TaskFromDBDAO}
     */
    protected void setLastActivation() {
        GDB.get(TaskFromDBDAO.class).setLastActivation(id);
    }
}
