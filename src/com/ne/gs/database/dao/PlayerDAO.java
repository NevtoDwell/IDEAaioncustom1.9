/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ne.gs.model.Race;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.world.WorldPosition;

/**
 * Class that is responsible for storing/loading player data
 *
 * @author SoulKeeper, Saelya
 * @author cura
 */
public abstract class PlayerDAO implements IDFactoryAwareDAO {

    /**
     * Returns true if name is used, false in other case
     *
     * @param name
     *     name to check
     *
     * @return true if name is used, false in other case
     */
    public abstract boolean isNameUsed(String name);

    /**
     * Returns PlayerObjectId by players name
     *
     * @param name
     *
     * @return PlayerObjectId
     */
    public abstract Integer getPlayerObjectIdByName(String name);

    /**
     * Returns player name by player object id
     *
     * @param playerObjectIds
     *     player object ids to get name
     *
     * @return map ObjectID-To-Name
     */
    public abstract Map<Integer, String> getPlayerNames(Collection<Integer> playerObjectIds);

    /**
     * Stores player to db
     *
     * @param player
     */
    public abstract void storePlayer(Player player);

    /**
     * This method is used to store only newly created characters
     *
     * @param pcd
     *     player to save in database
     *
     * @return true if every things went ok.
     */
    public abstract boolean saveNewPlayer(PlayerCommonData pcd, int accountId, String accountName);

    public abstract PlayerCommonData loadPlayerCommonData(int playerObjId);

    /**
     * Removes player and all related data (Done by CASCADE DELETION)
     *
     * @param playerId
     *     player to delete
     */
    public abstract void deletePlayer(int playerId);

    public abstract void updateDeletionTime(int objectId, Timestamp deletionDate);

    public abstract void storeCreationTime(int objectId, Timestamp creationDate);

    /**
     * Loads creation and deletion time from database, for particular player and sets these values in given <tt>PlayerAccountData</tt> object.
     *
     * @param acData
     */
    public abstract void setCreationDeletionTime(PlayerAccountData acData);

    /**
     * Returns a list of objectId of players that are on the account with given accountId
     *
     * @param accountId
     *
     * @return List<Integer>
     */
    public abstract List<Integer> getPlayerOidsOnAccount(int accountId);

    /**
     * Stores the last online time
     *
     * @param objectId
     *     Object ID of player to store
     * @param lastOnline
     *     Last online time of player to store
     */
    public abstract void storeLastOnlineTime(int objectId, Timestamp lastOnline);

    /**
     * Store online or offline player status
     *
     * @param player
     * @param online
     */
    public abstract void onlinePlayer(Player player, boolean online);

    /**
     * Set all players offline status
     *
     * @param online
     */
    public abstract void setPlayersOffline(boolean online);

    /**
     * get commondata by name for MailService
     *
     * @param name
     *
     * @return
     */
    public abstract PlayerCommonData loadPlayerCommonDataByName(String name);

    /**
     * Returns Player's Account ID
     *
     * @param name
     *
     * @return
     */
    public abstract int getAccountIdByName(String name);

    /**
     * Identifier name for all PlayerDAO classes
     *
     * @return PlayerDAO.class.getName()
     */
    public abstract String getPlayerNameByObjId(int playerObjId);

    /**
     * get playerId by name
     *
     * @param playerName
     *
     * @return
     */
    public abstract int getPlayerIdByName(String playerName);

    public abstract void storePlayerName(PlayerCommonData recipientCommonData);

    public abstract void setPlayerPosition(int objId, WorldPosition position);

    /**
     * Return account characters count
     *
     * @param accountId
     *
     * @return
     */
    public abstract int getCharacterCountOnAccount(int accountId);

    /**
     * Get characters count for a given Race
     *
     * @param race
     *
     * @return the number of characters for race
     */
    public abstract int getCharacterCountForRace(Race race);

    /**
     * Return online characters count
     *
     * @return
     */
    public abstract int getOnlinePlayerCount();

    public abstract List<Integer> getPlayersToDelete(int paramInt1, int paramInt2);

    public abstract void setPlayerLastTransferTime(int playerId, long time);

    @Override
    public final String getClassName() {
        return PlayerDAO.class.getName();
    }
}
