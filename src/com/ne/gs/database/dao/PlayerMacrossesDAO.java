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
import com.ne.gs.model.gameobjects.player.MacroList;

/**
 * Macrosses DAO
 * <p/>
 * Created on: 13.07.2009 17:05:56
 *
 * @author Aquanox
 */
public abstract class PlayerMacrossesDAO implements DAO {

    /**
     * Returns unique identifier for PlayerMacroDAO
     *
     * @return unique identifier for PlayerMacroDAO
     */
    @Override
    public final String getClassName() {
        return PlayerMacrossesDAO.class.getName();
    }

    /**
     * Returns a list of macrosses for player
     *
     * @param playerId
     *     Player object id.
     *
     * @return a list of macrosses for player
     */
    public abstract MacroList restoreMacrosses(int playerId);

    /**
     * Add a macro information into database
     *
     * @param playerId
     *     player object id
     * @param macroPosition
     *     macro order # of player
     * @param macro
     *     macro contents.
     */
    public abstract void addMacro(int playerId, int macroPosition, String macro);

    /**
     * Update a macro information into database
     *
     * @param playerId
     *     player object id
     * @param macroPosition
     *     macro order # of player
     * @param macro
     *     macro contents.
     */
    public abstract void updateMacro(int playerId, int macroPosition, String macro);

    /**
     * Remove macro in database
     *
     * @param playerId
     *     player object id
     * @param macroPosition
     *     order of macro in macro list
     */
    public abstract void deleteMacro(int playerId, int macroPosition);
}
