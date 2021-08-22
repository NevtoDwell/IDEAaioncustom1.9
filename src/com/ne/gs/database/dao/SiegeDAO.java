/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.Map;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.siege.SiegeLocation;

/**
 * @author Sarynth
 */
public abstract class SiegeDAO implements DAO {

    @Override
    public final String getClassName() {
        return SiegeDAO.class.getName();
    }

    public abstract boolean loadSiegeLocations(Map<Integer, SiegeLocation> locations);

    public abstract boolean updateSiegeLocation(SiegeLocation paramSiegeLocation);
}
