/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.tasks;

import java.sql.Timestamp;

/**
 * @author Divinity
 */
public class TaskFromDB {

    private final int id;
    private final String name;
    private final String type;
    private final Timestamp lastActivation;
    private final String startTime;
    private final int delay;
    private final String params[];

    /**
     * Constructor
     *
     * @param id
     *     : int
     * @param name
     *     : String
     * @param type
     *     : String
     * @param lastActivation
     *     : Timestamp
     * @param startTime
     *     : String
     * @param delay
     *     : int
     * @param param
     *     : String
     */
    public TaskFromDB(int id, String name, String type, Timestamp lastActivation, String startTime, int delay,
                      String param) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.lastActivation = lastActivation;
        this.startTime = startTime;
        this.delay = delay;

        if (param != null) {
            params = param.split(" ");
        } else {
            params = new String[0];
        }
    }

    /**
     * Task's id
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Task's name
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Task's type : - FIXED_IN_TIME (HH:MM:SS)
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Task's last activation
     *
     * @return Timestamp
     */
    public Timestamp getLastActivation() {
        return lastActivation;
    }

    /**
     * Task's starting time (HH:MM:SS format)
     *
     * @return String
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Task's delay
     *
     * @return int
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Task's param(s)
     *
     * @return String[]
     */
    public String[] getParams() {
        return params;
    }
}
