/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import com.ne.commons.database.dao.DAO;

/**
 * @author Ben
 */
public abstract class ServerVariablesDAO implements DAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getClassName() {
        return ServerVariablesDAO.class.getName();
    }

    /**
     * Loads the server variables stored in the database
     *
     * @returns variable stored in database
     */
    public abstract int load(String var);

    /**
     * Stores the server variables
     */
    public abstract boolean store(String var, int value);

}
