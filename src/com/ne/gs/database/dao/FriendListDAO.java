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
import com.ne.gs.model.gameobjects.player.FriendList;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author Ben
 */
public abstract class FriendListDAO implements DAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassName() {
        return FriendListDAO.class.getName();
    }

    /**
     * Loads the friend list for the given player
     *
     * @param player
     *     Player to get friend list of
     *
     * @return FriendList for player
     */
    public abstract FriendList load(Player player);

    /**
     * Makes the given players friends
     * <ul>
     * <li>Note: Adds for both players</li>
     * </ul>
     *
     * @param player
     *     Player who is adding
     * @param friend
     *     Friend to add to the friend list
     *
     * @return Success
     */
    public abstract boolean addFriends(Player player, Player friend);

    /**
     * Deletes the friends from eachothers lists
     *
     * @return Success
     */
    public abstract boolean delFriends(int playerOid, int friendOid);

}
