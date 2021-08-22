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
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.skill.PlayerSkillList;

/**
 * Created on: 15.07.2009 19:33:07 Edited On: 13.09.2009 19:48:00
 *
 * @author IceReaper, orfeo087, Avol, AEJTester
 */
public abstract class PlayerSkillListDAO implements DAO {

    /**
     * Returns unique identifier for PlayerSkillListDAO
     *
     * @return unique identifier for PlayerSkillListDAO
     */
    @Override
    public final String getClassName() {
        return PlayerSkillListDAO.class.getName();
    }

    /**
     * Returns a list of skilllist for player
     *
     * @param playerId
     *     Player object id.
     *
     * @return a list of skilllist for player
     */
    public abstract PlayerSkillList loadSkillList(int playerId);

    /**
     * Updates skill with new information
     *
     */
    public abstract boolean storeSkills(Player player);

}
