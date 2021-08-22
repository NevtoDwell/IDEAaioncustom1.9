/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.database.GDB;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.model.gameobjects.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.EventNotifier;
import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.database.dao.ItemCooldownsDAO;
import com.ne.gs.database.dao.PlayerCooldownsDAO;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.database.dao.PlayerEffectsDAO;
import com.ne.gs.database.dao.PlayerLifeStatsDAO;
import com.ne.gs.model.events.PlayerLeftGame;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.FriendList;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.model.summons.UnsummonType;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.network.aion.clientpackets.CM_QUIT;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.AutoGroupService2;
import com.ne.gs.services.BrokerService;
import com.ne.gs.services.ChatService;
import com.ne.gs.services.DuelService;
import com.ne.gs.services.ExchangeService;
import com.ne.gs.services.FindGroupService;
import com.ne.gs.services.KiskService;
import com.ne.gs.services.LegionService;
import com.ne.gs.services.PunishmentService;
import com.ne.gs.services.RepurchaseService;
import com.ne.gs.services.custom.OnlineBonusService;
import com.ne.gs.services.drop.DropService;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.services.summons.SummonsService;
import com.ne.gs.services.toypet.PetSpawnService;
import com.ne.gs.taskmanager.tasks.ExpireTimerTask;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.audit.GMService;

/**
 * @author ATracer
 */
public final class PlayerLeaveWorldService {

    private static final Logger log = LoggerFactory.getLogger(PlayerLeaveWorldService.class);

    /**
     * @param player
     * @param delay
     */
    public static void startLeaveWorldDelay(final Player player, int delay) {
        // force stop movement of player
        player.getController().stopMoving();

        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                startLeaveWorld(player);
            }
        }, delay);
    }

    /**
     * This method is called when player leaves the game, which includes just two cases: either player goes back to char selection screen or it's leaving the
     * game [closing client].<br>
     * <br>
     * <b><font color='red'>NOTICE: </font> This method is called only from GameConnection and {@link CM_QUIT} and must not be called from anywhere
     * else</b>
     */
    public static void startLeaveWorld(Player player) {
        log.info("Player logged out: " + player.getName() + " Account: "
            + (player.getClientConnection() != null ? player.getClientConnection().getAccount().getName() : "disconnected"));
        FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x00, player.getObjectId());
        FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x04, player.getObjectId());
        player.getResponseRequester().denyAll();
        player.getFriendList().setStatus(FriendList.Status.OFFLINE);
        BrokerService.getInstance().removePlayerCache(player);
        ExchangeService.getInstance().cancelExchange(player);
        RepurchaseService.getInstance().removeRepurchaseItems(player);
        AutoGroupService2.getInstance().onPlayerLogOut(player);
        InstanceService.onLogOut(player);
        GMService.getInstance().onPlayerLogedOut(player);
        KiskService.getInstance().onLogout(player);

        if (player.isLooting()) {
            DropService.getInstance().closeDropList(player, player.getLootingNpcOid());
        }

        // Update prison timer
        if (player.isInPrison()) {
            long prisonTimer = System.currentTimeMillis() - player.getStartPrison();
            prisonTimer = player.getPrisonTimer() - prisonTimer;
            player.setPrisonTimer(prisonTimer);
            log.debug("Update prison timer to " + prisonTimer / 1000 + " seconds !");
        }
        // Update gag timer
        if (player.isGag()) {
            long gagTimer = System.currentTimeMillis() - player.getStartGag();
            gagTimer = player.getGagTimer() - gagTimer;
            player.setGagTimer(gagTimer);
            log.debug("Update gag timer to " + gagTimer / 1000 + " seconds !");
        }
        // store current effects
        GDB.get(PlayerEffectsDAO.class).storePlayerEffects(player);
        GDB.get(PlayerCooldownsDAO.class).storePlayerCooldowns(player);
        GDB.get(ItemCooldownsDAO.class).storeItemCooldowns(player);
        GDB.get(PlayerLifeStatsDAO.class).updatePlayerLifeStat(player);
        // fix legion warehouse exploits
        LegionService.getInstance().LegionWhUpdate(player);
        player.getEffectController().removeAllEffects(true);
        player.getLifeStats().cancelAllTasks();

        if (player.getLifeStats().isAlreadyDead()) {
            if (player.isInInstance()) {
                PlayerReviveService.instanceRevive(player);
            } else {
                PlayerReviveService.bindRevive(player);
            }
        } else if (DuelService.getInstance().isDueling(player.getObjectId())) {
            DuelService.getInstance().loseDuel(player);
        }

        Summon summon = player.getSummon();
        if (summon != null) {
            SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.LOGOUT);
        }
        PetSpawnService.dismissPet(player, true);

        if (player.getPostman() != null) {
            player.getPostman().getController().onDelete();
        }
        player.setPostman(null);

        PunishmentService.stopPrisonTask(player, true);
        PunishmentService.stopGagTask(player, true);
        PunishmentService.stopGatherableTask(player, true);

        if (player.isLegionMember()) {
            LegionService.getInstance().onLogout(player);
        }

        PlayerGroupService.onPlayerLogout(player);
        PlayerAllianceService.onPlayerLogout(player);

        QuestEngine.getInstance().onLogOut(new QuestEnv(null, player, 0, 0));
        EventNotifier.GLOBAL.fire(PlayerLeftGame.class, player);
        player.getNotifier().fire(PlayerLeftGame.class, player);

        OnlineBonusService.getInstance().onLogout(player);

        player.getController().delete();
        player.getCommonData().setOnline(false);
        player.getCommonData().setLastOnline(new Timestamp(System.currentTimeMillis()));
        player.setClientConnection(null);

        GDB.get(PlayerDAO.class).onlinePlayer(player, false);

        if (GSConfig.ENABLE_CHAT_SERVER) {
            ChatService.onPlayerLogout(player);
        }

        PlayerService.storePlayer(player);

        ExpireTimerTask.getInstance().removePlayer(player);
        if (player.getCraftingTask() != null) {
            player.getCraftingTask().stop(true);
        }
        player.getEquipment().setOwner(null);
        player.getInventory().setOwner(null);
        player.getWarehouse().setOwner(null);
        player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId()).setOwner(null);

        List<Item> equipment;
        if(player.getEquipment() != null)
            equipment = player.getEquipment().getEquippedItems();
        else
            equipment = new ArrayList<>();

        PlayerAccountData pad = player.getPlayerAccount().getPlayerAccountData(player.getObjectId());
        pad.setEquipment(equipment);
    }

    /**
     * @param player
     */
    public static void tryLeaveWorld(Player player) {
        player.getMoveController().abortMove();
        if (player.getController().isInShutdownProgress()) {
            PlayerLeaveWorldService.startLeaveWorld(player);
        } else {
            int delay = 15;
            PlayerLeaveWorldService.startLeaveWorldDelay(player, (delay * 1000));
        }
    }
}
