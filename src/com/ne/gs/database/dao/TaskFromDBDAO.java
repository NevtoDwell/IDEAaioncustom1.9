/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.ArrayList;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.tasks.TaskFromDB;

/**
 * @author Divinity
 */
public abstract class TaskFromDBDAO implements DAO {

    /**
     * Return all tasks from GDB
     *
     * @return all tasks
     */
    public abstract ArrayList<TaskFromDB> getAllTasks();

    /**
     * Return all tasks with name from GDB
     *
     * @return all tasks with name;
     */
    public abstract ArrayList<TaskFromDB> getTasksByNameAndNpcId(String name, int npcId);

    /**
     * Set the last activation to NOW()
     */
    public abstract void setLastActivation(int id);

    /**
     * Set new start time
     */
    public abstract void setStartTime(String cron, int id);

    /**
     * Returns class name that will be uses as unique identifier for all DAO classes
     *
     * @return class name
     */
    @Override
    public final String getClassName() {
        return TaskFromDBDAO.class.getName();
    }
}
