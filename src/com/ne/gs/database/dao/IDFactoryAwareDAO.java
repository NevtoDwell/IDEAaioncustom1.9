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
 * This interface is generic one for all DAO classes that are generating their id's using {@link com.ne.gs.utils.idfactory.IDFactory}
 *
 * @author SoulKeeper
 */
public interface IDFactoryAwareDAO extends DAO {

    /**
     * Returns array of all id's that are used by this DAO
     *
     * @return array of used id's
     */
    public int[] getUsedIDs();
}
