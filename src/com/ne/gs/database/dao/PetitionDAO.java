/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.Set;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.Petition;

/**
 * @author zdead
 */
public abstract class PetitionDAO implements DAO {

    public abstract int getNextAvailableId();

    public abstract void insertPetition(Petition p);

    public abstract void deletePetition(int playerObjId);

    public abstract Set<Petition> getPetitions();

    public abstract Petition getPetitionById(int petitionId);

    public abstract void setReplied(int petitionId);

    @Override
    public final String getClassName() {
        return PetitionDAO.class.getName();
    }
}
