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

import com.ne.gs.model.team.legion.LegionMember;
import com.ne.gs.model.team.legion.LegionMemberEx;

/**
 * Class that is responsible for storing/loading legion data
 *
 * @author Simple
 */

public abstract class LegionMemberDAO implements IDFactoryAwareDAO {

    /**
     * Returns true if name is used, false in other case
     *
     * @return true if name is used, false in other case
     */
    public abstract boolean isIdUsed(int playerObjId);

    /**
     * Creates legion member in DB
     *
     * @param legionMember
     */
    public abstract boolean saveNewLegionMember(LegionMember legionMember);

    /**
     * Stores legion member to DB
     *
     */
    public abstract void storeLegionMember(int playerObjId, LegionMember legionMember);

    /**
     * Loads a legion member
     *
     * @param playerObjId
     * @return LegionMember
     */
    public abstract LegionMember loadLegionMember(int playerObjId);

    /**
     * Loads an off line legion member by id
     *
     * @param playerObjId
     * @return LegionMemberEx
     */
    public abstract LegionMemberEx loadLegionMemberEx(int playerObjId);

    /**
     * Loads an off line legion member by name
     *
     * @param playerName
     * @return LegionMemberEx
     */
    public abstract LegionMemberEx loadLegionMemberEx(String playerName);

    /**
     * Loads all legion members of a legion
     *
     * @param legionId
     *
     * @return ArrayList<Integer>
     */
    public abstract ArrayList<Integer> loadLegionMembers(int legionId);

    /**
     * Removes legion member and all related data (Done by CASCADE DELETION)
     *
     */
    public abstract void deleteLegionMember(int playerObjId);

    /**
     * Identifier name for all LegionDAO classes
     *
     * @return LegionDAO.class.getName()
     */
    @Override
    public final String getClassName() {
        return LegionMemberDAO.class.getName();
    }

}
