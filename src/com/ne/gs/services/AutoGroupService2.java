/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.network.util.ThreadPoolManager;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.DredgionConfig;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.instance.InstanceEngine;
import com.ne.gs.model.Race;
import com.ne.gs.model.autogroup.AutoGroupsType;
import com.ne.gs.model.autogroup.AutoInstance;
import com.ne.gs.model.autogroup.EntryRequestType;
import com.ne.gs.model.autogroup.LookingForParty;
import com.ne.gs.model.autogroup.SearchInstance;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.instancereward.PvPArenaReward;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.model.templates.portal.PortalLoc;
import com.ne.gs.model.templates.portal.PortalPath;
import com.ne.gs.network.aion.serverpackets.SM_AUTO_GROUP;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.services.instance.PvPArenaService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMap;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.WorldMapInstanceFactory;

import java.util.Collection;

/**
 * @author xTz
 */
public class AutoGroupService2 {

    private static final Logger log = LoggerFactory.getLogger("AUTOGROUP_LOG");
    private final FastMap<Integer, LookingForParty> playersSearcher = new FastMap<Integer, LookingForParty>().shared();
    private final FastMap<Integer, AutoInstance> playersInInstances = new FastMap<Integer, AutoInstance>().shared();

    private final Object lfpGuard = new Object();

    public void startLooking(Player player, byte instanceMaskId, EntryRequestType ert) {
        AutoGroupsType agt = AutoGroupsType.getAutoGroupByInstanceMaskId(instanceMaskId);
        if (agt == null) {
            return;
        }
        if (!canEnter(player, ert, agt)) {
            return;
        }
        LookingForParty lfp = getLookingForParty(player.getObjectId());
        if (lfp == null) {
            synchronized (lfpGuard) {
                playersSearcher.put(player.getObjectId(), new LookingForParty(player, instanceMaskId, ert));
            }
            if (agt == AutoGroupsType.ARENA_OF_CHAOS_3) {
                for (Player p : World.getInstance().getAllPlayers()) {
                    PacketSendUtility.sendBrightYellowMessageOnCenter(p,
                            "Игрок: " + player.getName()
                            + " зарегистрировался на Боевую Арену Хаоса (" + playersSearcher.size()
                            + "/" + CustomConfig.ARENA_OF_CHAOS_PLAYERS_SIZE + ")");
                }
            }
        } else if (!lfp.canRegister() || lfp.getInstanceMaskIds().contains(instanceMaskId)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400181, agt.getInstanceMapId()));
            return;
        } else {
            lfp.addInstanceMaskId(instanceMaskId, ert);
        }
        if (ert.isGroupEntry()) {
            for (Player member : player.getPlayerGroup2().getOnlineMembers()) {
                member.sendPck(new SM_SYSTEM_MESSAGE(1400194, agt.getInstanceMapId()));
                member.sendPck(new SM_AUTO_GROUP(instanceMaskId, 1, ert.getId(), player.getName()));
            }
        } else {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400194, agt.getInstanceMapId()));
            player.sendPck(new SM_AUTO_GROUP(instanceMaskId, 1, ert.getId(), player.getName()));
        }
        if (LoggingConfig.LOG_AUTOGROUP) {
            log.info("[AUTOGROUPSERVICE] > Register playerName: " + player.getName() + " class: " + player.getPlayerClass() + " race: " + player.getRace());
            log.info("[AUTOGROUPSERVICE] > Register instanceMaskId: " + instanceMaskId + " type: " + ert);
        }
        startSort(ert, instanceMaskId, player.getRace());
    }

    public void unregisterLooking(Player player, byte instanceMaskId) {
        if (LoggingConfig.LOG_AUTOGROUP) {
            log.info("[AUTOGROUPSERVICE] > unregisterLooking instanceMaskId: " + instanceMaskId + " player: " + player.getName());
        }
        AutoGroupsType agt = AutoGroupsType.getAutoGroupByInstanceMaskId(instanceMaskId);
        LookingForParty lfp = getLookingForParty(player.getObjectId());
        if (agt == null || lfp == null) {
            return;
        }
        SearchInstance searchInstance = lfp.getSearchInstance(instanceMaskId);
        if (searchInstance == null) {
            return;
        }
        if (searchInstance.getEntryRequestType().isGroupEntry() && player.isInGroup2()) {
            for (Player member : player.getPlayerGroup2().getOnlineMembers()) {
                member.sendPck(new SM_AUTO_GROUP(instanceMaskId, 2));
            }
        } else {
            player.sendPck(new SM_AUTO_GROUP(instanceMaskId, 2));
        }
        startRejectTask(player.getObjectId(), instanceMaskId);
    }

    public void cancelEnter(Player player, byte instanceMaskId) {
        if (LoggingConfig.LOG_AUTOGROUP) {
            log.info("[AUTOGROUPSERVICE] > cancelEnter requestEntryId: " + instanceMaskId + " player: " + player.getName());
        }
        AutoGroupsType agt = AutoGroupsType.getAutoGroupByInstanceMaskId(instanceMaskId);
        LookingForParty lfp = getLookingForParty(player.getObjectId());
        AutoInstance autoInstance = getAutoInstance(player, instanceMaskId);
        if (agt == null || autoInstance == null) {
            return;
        }

        if (lfp != null) {
            SearchInstance searchInstance = lfp.getSearchInstance(instanceMaskId);
            if (searchInstance == null) {
                return;
            }
            if (searchInstance.getEntryRequestType().isGroupEntry()) {
                for (Player member : autoInstance.getPlayers()) {
                    if (!member.equals(player) && member.getRace().equals(player.getRace())) {
                        autoInstance.unregisterPlayer(player);
                        member.sendPck(new SM_AUTO_GROUP(instanceMaskId, 2));
                    }
                }
            }
            startRejectTask(player.getObjectId(), instanceMaskId);
        } else {
            autoInstance.unregisterPlayer(player);
        }
    }

    public void onPlayerLogin(Player player) {
        LookingForParty lfp = getLookingForParty(player.getObjectId());
        if (lfp != null) {
            lfp.setPlayer(player);
            for (SearchInstance searchInstance : lfp.getSearchInstances()) {
                player.sendPck(new SM_AUTO_GROUP(searchInstance.getInstanceMaskId(), 8, searchInstance.getRemainingTime()
                        + searchInstance.getEntryRequestType().getId(), player.getName()));
                if (searchInstance.isDredgion()) {
                    player.sendPck(new SM_AUTO_GROUP(searchInstance.getInstanceMaskId(), 6, true));
                }
                startSort(searchInstance.getEntryRequestType(), searchInstance.getInstanceMaskId(), player.getRace());
            }
        }
        if (player.isInGroup2()) {
            Player leader = player.getPlayerGroup2().getLeaderObject();

            synchronized (lfpGuard) {
                LookingForParty groupLfp = playersSearcher.get(leader.getObjectId());
                if (groupLfp == null) {
                    return;
                }
                for (SearchInstance searchInstance : groupLfp.getSearchInstances()) {
                    if (!searchInstance.getEntryRequestType().isGroupEntry()) {
                        continue;
                    }
                    player.sendPck(new SM_AUTO_GROUP(searchInstance.getInstanceMaskId(), 8, searchInstance.getRemainingTime()
                            + searchInstance.getEntryRequestType().getId(), leader.getName()));
                }
            }
        }
    }

    public void onPlayerLogOut(Player player) {
        LookingForParty lfp = getLookingForParty(player.getObjectId());
        if (lfp != null) {
            lfp.setPlayer(null);
        }
        for (AutoInstance autoInstance : playersInInstances.values()) {
            if (autoInstance.containPlayer(player) && autoInstance.getInstanceId() == player.getInstanceId()) {
                if (autoInstance.getWorldMapInstance().getPlayersInside().size() < 2) {
                    playersInInstances.remove(autoInstance.getInstanceId());
                    InstanceService.destroyInstance(autoInstance.getWorldMapInstance());
                }
            }
        }
    }

    public void onLeaveInstance(Player player) {
        if (player.isInInstance()) {
            AutoInstance autoInstance = playersInInstances.get(player.getInstanceId());
            if (autoInstance == null) {
                return;
            }
            autoInstance.unregisterPlayer(player);
            destroyAutoInstance(autoInstance);
            PlayerGroupService.removePlayer(player);
            startSort(EntryRequestType.QUICK_GROUP_ENTRY, autoInstance.getInstanceMaskId(), player.getRace());
        }
    }

    private void destroyAutoInstance(AutoInstance autoInstance) {
        if (autoInstance.getPlayers().isEmpty()) {
            playersInInstances.remove(autoInstance.getInstanceId());
            InstanceService.destroyInstance(autoInstance.getWorldMapInstance());
        }
    }

    private AutoInstance getAutoInstance(Player player, byte instanceMaskId) {
        for (AutoInstance autoInstance : playersInInstances.values()) {
            if (autoInstance.hasInstanceMask(instanceMaskId) && autoInstance.containPlayer(player)) {
                return autoInstance;
            }
        }
        return null;
    }

    public void enterToInstance(Player player, byte instanceMaskId) {

        if (player.isAttackMode()) {
            // to do msg
            return;
        }
        AutoGroupsType agt = AutoGroupsType.getAutoGroupByInstanceMaskId(instanceMaskId);
        if (agt == null) {
            return;
        }
        AutoInstance autoInstance = getAutoInstance(player, instanceMaskId);
        if (autoInstance == null) {
            return;
        }
        LookingForParty lfp = getLookingForParty(player.getObjectId());
        if (lfp != null) {
            if (!lfp.isRegistredInstance(instanceMaskId) || !lfp.isInvited(instanceMaskId) || !lfp.isOnStartEnterTask()) {
                return;
            }
            SearchInstance searchInstance = lfp.getSearchInstance(instanceMaskId);
            if (searchInstance.getEntryRequestType().isGroupEntry()) {
                if (!player.isInGroup2()) {

                    synchronized (lfpGuard) {
                        playersSearcher.remove(player.getObjectId());
                    }
                    autoInstance.unregisterPlayer(player);
                    return;
                }
                if (!autoInstance.getPlayersInside().contains(player)) {
                    sendEnter(player.getPlayerGroup2(), instanceMaskId, autoInstance);
                }
            }
        }
        if (autoInstance.getPlayersInside().contains(player)) {
            log.warn("[AUTOGROUPSERVICE] > is inside player: " + player.getName() + " instanceMaskId " + instanceMaskId);
            return;
        }
        if (player.isInGroup2()) {
            PlayerGroupService.removePlayer(player);
        }
        if (player.isInAlliance2()) {
            PlayerAllianceService.removePlayer(player);
        }
        int worldId = agt.getInstanceMapId();
        int instanceId = autoInstance.getInstanceId();
        if (agt.isPvpArena()) {
            if (agt.isPvPFFAArena() || agt.isPvPSoloArena()) {
                if (agt.isPvPFFAArena() && CustomConfig.ARENA_OF_CHAOS_TICKET_CHECK
                        && !player.getInventory().decreaseByItemId(186000135, 1)) {
                    return;
                }
                if (agt.isPvPSoloArena() && CustomConfig.PVP_SOLO_ARENA_TICKET_CHECK
                        && !player.getInventory().decreaseByItemId(186000135, 1)) {
                    return;
                }
            }
            ((PvPArenaReward) autoInstance.getWorldMapInstance().getInstanceHandler().getInstanceReward()).portToPosition(player, false);
            autoInstance.getWorldMapInstance().register(player.getObjectId());
            // to do poity v pisdu
        } else {
            PortalPath portal = DataManager.PORTAL2_DATA.getPortalDialog(worldId, 10000, player.getRace());
            if (portal == null) {
                return;
            }
            PortalLoc loc = DataManager.PORTAL_LOC_DATA.getPortalLoc(portal.getLocId());
            if (loc == null) {
                return;
            }
            TeleportService.teleportTo(player, worldId, instanceId, loc.getX(), loc.getY(), loc.getZ(), loc.getH());
            int instanceCooldownRate = InstanceService.getInstanceRate(player, worldId);
            int instanceCoolTime = DataManager.INSTANCE_COOLTIME_DATA.getInstanceEntranceCooltime(worldId);
            player.getPortalCooldownList().addPortalCooldown(worldId, instanceCoolTime * 60 * 1000 / instanceCooldownRate);
        }

        if (!agt.isPvpArena()) {
            autoInstance.enterToGroup(player);
        }
        if (lfp != null && lfp.unregisterInstance(instanceMaskId) == 0) {

            synchronized (lfpGuard) {
                playersSearcher.remove(player.getObjectId());
            }
        }
        player.sendPck(new SM_AUTO_GROUP(instanceMaskId, 5));
    }

    private void startRejectTask(final Integer object, final byte instanceMaskId) {
        final LookingForParty lfp = getLookingForParty(object);
        if (lfp == null) {
            return;
        }
        lfp.setRejecRegistration(false);
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                lfp.setRejecRegistration(true);
                lfp.clearStartEnterTime();
                removeLooking(object, instanceMaskId);
            }
        }, 10000);
    }

    private void removeLooking(Integer object, byte instanceMaskId) {
        LookingForParty lfp = getLookingForParty(object);
        if (lfp == null) {
            return;
        }
        Player player = lfp.getPlayer();
        if (lfp.getPlayer() != null) {
            unregisterPlayerFromAutoInstance(player, instanceMaskId);
        }

        if (lfp.isRegistredInstance(instanceMaskId) && lfp.unregisterInstance(instanceMaskId) == 0) {
            synchronized (lfpGuard) {
                playersSearcher.remove(object);
            }
        }
        startSort(EntryRequestType.QUICK_GROUP_ENTRY, instanceMaskId, null);
    }

    private void unregisterPlayerFromAutoInstance(Player player, byte instanceMaskId) {
        AutoInstance autoInstance = getAutoInstance(player, instanceMaskId);
        if (autoInstance == null) {
            return;
        }
        autoInstance.unregisterPlayer(player);
        destroyAutoInstance(autoInstance);
    }

    private synchronized void startSort(EntryRequestType ert, byte instanceMaskId, Race race) {
        AutoGroupsType agt = AutoGroupsType.getAutoGroupByInstanceMaskId(instanceMaskId);
        AutoInstance autoInstance;
        switch (ert) {
            case NEW_GROUP_ENTRY:
            case QUICK_GROUP_ENTRY:
                Collection<LookingForParty> lfpColl;
                synchronized (lfpGuard){
                    lfpColl = playersSearcher.values();
                }
                for (LookingForParty lfp : lfpColl) {
                    Player player = lfp.getPlayer();
                    if (player == null || lfp.isOnStartEnterTask()) {
                        continue;
                    }
                    lab:
                    for (SearchInstance searchInstance : lfp.getSearchInstances()) {
                        for (AutoInstance instance : playersInInstances.values()) {
                            if (searchInstance.getInstanceMaskId() != instance.getInstanceMaskId()
                                    || !instance.hasRacePermit(player.getRace())
                                    || !instance.satisfyTime(ert)) {
                                continue;
                            }
                            if (instance.canAddPlayer(player)) {
                                if (LoggingConfig.LOG_AUTOGROUP) {
                                    log.info("[AUTOGROUPSERVICE] > sort QUICK_GROUP_ENTRY player: " + player.getName());
                                }
                                lfp.setInvited(instance.getInstanceMaskId(), true);
                                lfp.setStartEnterTime();
                                sendEnter(player, instance.getInstanceMaskId());
                                break lab;
                            }
                        }
                    }
                }
                if (race == null) {
                    return;
                }
                autoInstance = new AutoInstance(agt.isPvpArena() ? Race.PC_ALL : race, instanceMaskId, null, ert);
                for (LookingForParty lfp : playersSearcher.values()) {
                    Player player = lfp.getPlayer();
                    SearchInstance searchInstance = lfp.getSearchInstance(instanceMaskId);
                    if (searchInstance == null || searchInstance.getEntryRequestType().isGroupEntry()) {
                        continue;
                    }
                    if (player != null && !lfp.isInvited(instanceMaskId)
                            && autoInstance.hasRacePermit(player.getRace()) && !lfp.isOnStartEnterTask()) {
                        if (autoInstance.canAddPlayer(player) && autoInstance.hasSizePermit()) {
                            break;
                        }
                    }
                }

                if (autoInstance.hasSizePermit()) {
                    WorldMapInstance instance = createInstance(agt.getInstanceMapId(), agt.getDifficultId());
                    autoInstance.setWorldMapInstance(instance);
                    playersInInstances.put(instance.getInstanceId(), autoInstance);
                    for (Player player : autoInstance.getPlayers()) {
                        if (LoggingConfig.LOG_AUTOGROUP) {
                            log.info("[AUTOGROUPSERVICE] > sort NEW_GROUP_ENTRY player: " + player.getName());
                        }
                        LookingForParty lfp = getLookingForParty(player.getObjectId());
                        if (lfp != null) {
                            lfp.setInvited(instanceMaskId, true);
                            lfp.setStartEnterTime();
                            //Убрана табличка подтверждения на вход при достижении нужного ко-ва участников
                            //Чтоб вернуть - вырезать if (agt.isPvpArena()) или enterToInstance заменить на sendEnter
                            if (agt.isPvpArena() || agt.isDredgion()) {
                                enterToInstance(player, instanceMaskId);
                            } else {
                                sendEnter(player, instanceMaskId);
                            }
                        }
                    }
                }
                break;
            case GROUP_ENTRY:
                break;
        }
    }

    private LookingForParty getLookingForParty(Race race, byte instanceMaskId, EntryRequestType ert) {

        Collection<LookingForParty> lfpColl;
        synchronized (lfpGuard){
            lfpColl = playersSearcher.values();
        }

        for (LookingForParty lfp : lfpColl) {
            Player player = lfp.getPlayer();
            SearchInstance searchInstance = lfp.getSearchInstance(instanceMaskId);
            if (searchInstance == null || searchInstance.getEntryRequestType().getId() != ert.getId()) {
                continue;
            }
            if (player != null && (player.getRace().equals(race) || race.equals(Race.PC_ALL)) && !lfp.isInvited(instanceMaskId) && !lfp.isOnStartEnterTask()) {
                return lfp;
            }
        }
        return null;
    }

    public void unregisterInstance(AutoGroupsType agt) {
        Collection<LookingForParty> lfpColl;
        synchronized (lfpGuard){
            lfpColl = playersSearcher.values();
        }
        for (LookingForParty lfp : lfpColl) {
            Player player = lfp.getPlayer();
            byte maskId = agt.getInstanceMaskId();
            if (player != null) {
                player.sendPck(new SM_AUTO_GROUP(maskId, 2));
            }
            if (!lfp.isInvited(maskId) && lfp.unregisterInstance(maskId) == 0) {

                synchronized (lfpGuard) {
                    playersSearcher.values().remove(lfp);
                }
            }
        }
    }

    private void sendEnter(PlayerGroup group, byte instanceMaskId, AutoInstance autoInstance) {
        for (Player player : group.getOnlineMembers()) {
            if (!autoInstance.containPlayer(player)) {
                autoInstance.addPlayer(player);
                sendEnter(player, instanceMaskId);
            }
        }
    }

    private void sendEnter(Player player, byte instanceMaskId) {
        if (LoggingConfig.LOG_AUTOGROUP) {
            log.info("[AUTOGROUPSERVICE] > sendEnter player: " + player.getName() + " instanceMaskId: " + instanceMaskId);
        }
        player.sendPck(new SM_AUTO_GROUP(instanceMaskId, 4));
    }

    public void sendRequestEntry(Player player, int npcId) {
        AutoGroupsType agt = AutoGroupsType.getAutoGroup(player.getLevel(), npcId);
        if (agt != null) {
            player.sendPck(new SM_AUTO_GROUP(agt.getInstanceMaskId()));
        }
    }

    private LookingForParty getLookingForParty(Integer object) {

        synchronized (lfpGuard) {
            return playersSearcher.get(object);
        }
    }

    private boolean canEnter(Player player, EntryRequestType ert, AutoGroupsType agt) {
        int mapId = agt.getInstanceMapId();
        if (!agt.hasLevelPermit(player.getLevel())) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400179, mapId));
            return false;
        }

        if (agt.isPvPFFAArena() || agt.isPvPSoloArena()) {
            if (agt.isPvPFFAArena() && CustomConfig.ARENA_OF_CHAOS_TICKET_CHECK && player.getInventory().getFirstItemByItemId(186000135) == null) {
                player.sendPck(new SM_SYSTEM_MESSAGE(1400219, mapId));
                return false;
            }
            if (agt.isPvPSoloArena() && CustomConfig.PVP_SOLO_ARENA_TICKET_CHECK && player.getInventory().getFirstItemByItemId(186000135) == null) {
                player.sendPck(new SM_SYSTEM_MESSAGE(1400219, mapId));
                return false;
            }
            if (!PvPArenaService.isPvPArenaAvailable(player, mapId)) {
                return false;
            }
        } else if (hasCoolDown(player, mapId)) {
            return false;
        }
        switch (ert) {
            case NEW_GROUP_ENTRY:
                if (!agt.hasRegisterNew()) {
                    return false;
                }
                break;
            case QUICK_GROUP_ENTRY:
                if (!agt.hasRegisterQuick()) {
                    return false;
                }
                break;
            case GROUP_ENTRY:
                if (!agt.hasRegisterGroup()) {
                    return false;
                }
                PlayerGroup group = player.getPlayerGroup2();
                if (group == null || !group.isLeader(player)) {
                    player.sendPck(new SM_SYSTEM_MESSAGE(1400182));
                    return false;
                }
                if (!group.isFull()) {
                    return false;
                }

                for (Player member : group.getMembers()) {
                    if (hasCoolDown(member, mapId)) {
                        return false;
                    }
                    if (!agt.hasLevelPermit(member.getLevel())) {
                        player.sendPck(new SM_SYSTEM_MESSAGE(1400179, mapId));
                        return false;
                    }
                }
                break;
        }
        return true;
    }

    private WorldMapInstance createInstance(int worldId, int difficultId) {
        if (LoggingConfig.LOG_AUTOGROUP) {
            log.info("[AUTOGROUPSERVICE] > createInstance: " + worldId);
        }
        WorldMap map = World.getInstance().getWorldMap(worldId);
        int nextInstanceId = map.getNextInstanceId();
        WorldMapInstance worldMapInstance = WorldMapInstanceFactory.createWorldMapInstance(map, nextInstanceId);
        map.addInstance(nextInstanceId, worldMapInstance);
        SpawnEngine.spawnInstance(worldId, worldMapInstance.getInstanceId(), difficultId);
        InstanceEngine.getInstance().onInstanceCreate(worldMapInstance);
        return worldMapInstance;
    }

    private boolean hasCoolDown(Player player, int worldId) {
        int instanceCooldownRate = InstanceService.getInstanceRate(player, worldId);
        int useDelay = 0;
        int instanceCooldown = DataManager.INSTANCE_COOLTIME_DATA.getInstanceEntranceCooltime(worldId);
        if (instanceCooldownRate > 0) {
            useDelay = instanceCooldown / instanceCooldownRate;
        }
        return player.getPortalCooldownList().isPortalUseDisabled(worldId) && useDelay > 0;
    }

    public static AutoGroupService2 getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder {

        protected static final AutoGroupService2 instance = new AutoGroupService2();
    }
}
