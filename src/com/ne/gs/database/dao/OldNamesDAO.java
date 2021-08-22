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
 * @author synchro2
 */
public abstract class OldNamesDAO implements DAO {

    public abstract boolean isOldName(String name);

    public abstract void insertNames(int id, String oldname, String newname);

    @Override
    public final String getClassName() {
        return OldNamesDAO.class.getName();
    }
}
