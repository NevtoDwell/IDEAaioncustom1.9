/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.Iterator;
import java.util.List;

import com.ne.gs.database.GDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.shared.AccountTime;
import com.ne.gs.GameServer;
import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.database.dao.InventoryDAO;
import com.ne.gs.database.dao.LegionMemberDAO;
import com.ne.gs.database.dao.PlayerAppearanceDAO;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.database.dao.PlayerPunishmentsDAO;
import com.ne.gs.model.Race;
import com.ne.gs.model.account.Account;
import com.ne.gs.model.account.CharacterBanInfo;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.PlayerAppearance;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.items.storage.PlayerStorage;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.team.legion.LegionMember;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.player.PlayerService;
import com.ne.gs.world.World;

/**
 * This class is a front-end for daos and it's responsibility is to retrieve the Account objects
 *
 * @author Luno
 * @modified cura
 */
public final class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    /**
     * Returns {@link Account} object that has given id.
     *
     * @param accountId
     * @param accountTime
     * @param accountName
     * @param accessLevel
     * @param membership
     *
     * @return Account
     */
    public static Account getAccount(int accountId, String accountName, AccountTime accountTime, byte accessLevel,
                                     byte membership, @Deprecated long toll, long expire) {
        log.debug("[AS] request for account: " + accountId);

        Account account = loadAccount(accountId);
        account.setName(accountName);
        account.setAccountTime(accountTime);
        account.setAccessLevel(accessLevel);
        account.setMembership(membership);
        account.setExpire(expire);

        removeDeletedCharacters(account);
        if (account.isEmpty()) {
            removeAccountWH(accountId);
        }

        return account;
    }

    /**
     * Removes from db characters that should be deleted (their deletion time has passed).
     *
     * @param account
     */
    private static void removeDeletedCharacters(Account account) {
        /* Removes chars that should be removed */
        Iterator<PlayerAccountData> it = account.iterator();
        while (it.hasNext()) {
            PlayerAccountData pad = it.next();
            Race race = pad.getPlayerCommonData().getRace();
            int deletionTime = pad.getDeletionTimeInSeconds() * 1000;
            if (deletionTime != 0 && deletionTime <= System.currentTimeMillis()) {
                it.remove();
                account.decrementCountOf(race);
                PlayerService.deletePlayerFromDB(pad.getPlayerCommonData().getPlayerObjId());
                if (GSConfig.ENABLE_RATIO_LIMITATION && pad.getPlayerCommonData().getLevel() >= GSConfig.RATIO_MIN_REQUIRED_LEVEL) {
                    if (account.getNumberOf(race) == 0) {
                        GameServer.updateRatio(pad.getPlayerCommonData().getRace(), -1);
                    }
                }
            }
        }
    }

    private static void removeAccountWH(int accountId) {
        GDB.get(InventoryDAO.class).deleteAccountWH(accountId);
    }

    /**
     * Loads account data and returns.
     *
     * @param accountId
     *
     * @return
     */
    public static Account loadAccount(int accountId) {
        Account account = new Account(accountId);

        PlayerDAO playerDAO = GDB.get(PlayerDAO.class);
        PlayerAppearanceDAO appereanceDAO = GDB.get(PlayerAppearanceDAO.class);

        List<Integer> playerIdList = playerDAO.getPlayerOidsOnAccount(accountId);

        for (int playerId : playerIdList) {
            PlayerCommonData playerCommonData = playerDAO.loadPlayerCommonData(playerId);
            CharacterBanInfo cbi = GDB.get(PlayerPunishmentsDAO.class).getCharBanInfo(playerId);
            if (playerCommonData.isOnline()) {
                if (World.getInstance().findPlayer(playerId) == null) {
                    playerCommonData.setOnline(false);
                    log.warn(playerCommonData.getName() + " has online status, but I cant find it in World. Skip online status");
                }
            }
            PlayerAppearance appereance = appereanceDAO.load(playerId);

            LegionMember legionMember = GDB.get(LegionMemberDAO.class).loadLegionMember(playerId);

            /**
             * Load only equipment and its stones to display on character selection screen
             */
            List<Item> equipment = GDB.get(InventoryDAO.class).loadEquipment(playerId);

            PlayerAccountData acData = new PlayerAccountData(playerCommonData, cbi, appereance, equipment, legionMember);
            playerDAO.setCreationDeletionTime(acData);

            account.addPlayerAccountData(acData);

            if (account.getAccountWarehouse() == null) {
                Storage accWarehouse = GDB.get(InventoryDAO.class).loadStorage(playerId, StorageType.ACCOUNT_WAREHOUSE);
                ItemService.loadItemStones(accWarehouse.getItems());
                account.setAccountWarehouse(accWarehouse);
            }
        }

        // For new accounts - create empty account warehouse
        if (account.getAccountWarehouse() == null) {
            account.setAccountWarehouse(new PlayerStorage(StorageType.ACCOUNT_WAREHOUSE));
        }
        return account;
    }
}
