/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.instance;

import com.ne.commons.utils.ClassUtils;
import java.util.Iterator;

import javolution.util.FastList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.eventNewEngine.events.holders.IEventHolder;
import com.ne.gs.instance.InstanceEngine;
import com.ne.gs.instance.handlers.GeneralEventHandler;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.templates.world.WorldMapTemplate;
import com.ne.gs.network.aion.SystemMessageId;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.AutoGroupService2;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMap;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.WorldMapInstanceFactory;
import com.ne.gs.world.WorldMapType;
import com.ne.gs.world.zone.ZoneInstance;

/**
 * @author ATracer
 */
public final class InstanceService {

    private static final Logger log = LoggerFactory.getLogger(InstanceService.class);
    private static final FastList<Integer> instanceAggro = new FastList<>();
    private static final FastList<Integer> instanceCoolDownFilter = new FastList<>();

    public static void load() {
        for (String s : CustomConfig.INSTANCES_MOB_AGGRO.split(",")) {
            instanceAggro.add(Integer.parseInt(s));
        }
        for (String s : CustomConfig.INSTANCES_COOL_DOWN_FILTER.split(",")) {
            instanceCoolDownFilter.add(Integer.parseInt(s));
        }
    }

    /**
     * @param worldId
     *
     * @return
     */
    public static synchronized WorldMapInstance getNextAvailableInstance(int worldId, int ownerId) {
        WorldMap map = World.getInstance().getWorldMap(worldId);

        if (!map.isInstanceType()) {
            throw new UnsupportedOperationException("Invalid call for next available instance  of " + worldId);
        }

        int nextInstanceId = map.getNextInstanceId();

        log.info("Creating new instance:" + worldId + " id:" + nextInstanceId + " owner:" + ownerId);
        WorldMapInstance worldMapInstance = WorldMapInstanceFactory.createWorldMapInstance(map, nextInstanceId, ownerId);

        map.addInstance(nextInstanceId, worldMapInstance);
        SpawnEngine.spawnInstance(worldId, worldMapInstance.getInstanceId(), 0, ownerId);
        InstanceEngine.getInstance().onInstanceCreate(worldMapInstance);

        if (map.isInstanceType()) {
            startInstanceChecker(worldMapInstance);
        }

        return worldMapInstance;
    }
    
    public synchronized static WorldMapInstance getNextAvailableEventInstance(IEventHolder holder) {
        int worldId = holder.getEventType().getEventTemplate().getMapId();
        int eventHandlerId = holder.getEventType().getEventTemplate().getEventId();

        WorldMap map = World.getInstance().getWorldMap(worldId);

        if (!map.isInstanceType()) {
            throw new UnsupportedOperationException("Invalid call for next available instance  of " + worldId);
        }

        int nextInstanceId = map.getNextInstanceId();
        log.info("Creating new Event instance:" + worldId + " id:" + nextInstanceId + " eventId:" + eventHandlerId);
        WorldMapInstance worldMapInstance = WorldMapInstanceFactory.createEventWorldMapInstance(map, nextInstanceId, eventHandlerId);

        map.addInstance(nextInstanceId, worldMapInstance);
        // спавн инста отключен
        //SpawnEngine.spawnInstance(worldId, worldMapInstance.getInstanceId(), (byte) 0, 0);
        log.info(worldMapInstance.getInstanceHandler() + " is GeneralEventHandler " + ClassUtils.isSubclass(worldMapInstance.getInstanceHandler().getClass(), GeneralEventHandler.class));
        ((GeneralEventHandler) worldMapInstance.getGeneralEventHandler()).setEventType(holder.getEventType());
        InstanceEngine.getInstance().onInstanceCreate(worldMapInstance);

        // finally start the checker
        if (map.isInstanceType()) {
            startInstanceChecker(worldMapInstance);
        }

        return worldMapInstance;
    }
    

    public static synchronized WorldMapInstance getNextAvailableInstance(int worldId) {
        return getNextAvailableInstance(worldId, 0);
    }

    /**
     * Instance will be destroyed All players moved to bind location All objects - deleted
     */
    public static void destroyInstance(WorldMapInstance instance) {

        if (instance.getEmptyInstanceTask() != null) {
            instance.getEmptyInstanceTask().cancel(false);
        }

        instance.cancelTasks();

        int worldId = instance.getMapId();
        WorldMap map = World.getInstance().getWorldMap(worldId);
        if (!map.isInstanceType()) {
            return;
        }
        int instanceId = instance.getInstanceId();

        map.removeWorldMapInstance(instanceId);

        log.info("Destroying instance:" + worldId + " " + instanceId);

        Iterator<VisibleObject> it = instance.objectIterator();
        while (it.hasNext()) {
            VisibleObject obj = it.next();
            if (obj instanceof Player) {
                Player player = (Player) obj;
                player.sendPck(new SM_SYSTEM_MESSAGE(SystemMessageId.LEAVE_INSTANCE_NOT_PARTY));
                moveToExitPoint((Player) obj);
            } else {
                obj.getController().onDelete();
            }
        }
        instance.getInstanceHandler().destroy();
    }

    /**
     * @param instance
     * @param player
     */
    public static void registerPlayerWithInstance(WorldMapInstance instance, Player player) {
        Integer obj = player.getObjectId();
        instance.register(obj);
        instance.setSoloPlayerObj(obj);
    }

    /**
     * @param instance
     * @param group
     */
    public static void registerGroupWithInstance(WorldMapInstance instance, PlayerGroup group) {
        instance.registerGroup(group);
    }

    /**
     * @param instance
     * @param group
     */
    public static void registerAllianceWithInstance(WorldMapInstance instance, PlayerAlliance group) {
        instance.registerGroup(group);
    }

    /**
     * @param worldId
     * @param objectId
     *
     * @return instance or null
     */
    public static WorldMapInstance getRegisteredInstance(int worldId, int objectId) {
        Iterator<WorldMapInstance> iterator = World.getInstance().getWorldMap(worldId).iterator();
        while (iterator.hasNext()) {
            WorldMapInstance instance = iterator.next();
            if (instance.isRegistered(objectId)) {
                return instance;
            }
        }
        return null;
    }

    public static WorldMapInstance getPersonalInstance(int worldId, int ownerId) {
        if (ownerId == 0) {
            return null;
        }
        Iterator<WorldMapInstance> iterator = World.getInstance().getWorldMap(worldId).iterator();
        while (iterator.hasNext()) {
            WorldMapInstance instance = iterator.next();
            if ((instance.isPersonal()) && (instance.getOwnerId() == ownerId)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * @param player
     */
    public static void onPlayerLogin(Player player) {
        int worldId = player.getWorldId();
        WorldMapTemplate worldTemplate = DataManager.WORLD_MAPS_DATA.getTemplate(worldId);
        if (worldTemplate.isInstance()) {
            boolean isPersonal = WorldMapType.of(player.getWorldId()).isPersonal();
            int lookupId;
            SkillEngine.getInstance().applyEffectDirectly(2196, player, player, 10000);
            if (player.isInGroup2()) {
                lookupId = player.getPlayerGroup2().getTeamId();
            } else if (player.isInAlliance2()) {
                lookupId = player.getPlayerAlliance2().getTeamId();
            } else if (isPersonal && player.getCommonData().getWorldOwnerId() != 0) {
                lookupId = player.getCommonData().getWorldOwnerId();
            } else {
                lookupId = player.getObjectId();
            }
            WorldMapInstance registeredInstance = isPersonal ? getPersonalInstance(worldId, lookupId) : getRegisteredInstance(worldId, lookupId);

            if (isPersonal) {
                if (registeredInstance == null) {
                    registeredInstance = getNextAvailableInstance(player.getWorldId(), lookupId);
                }
                if (!registeredInstance.isRegistered(player.getObjectId())) {
                    registerPlayerWithInstance(registeredInstance, player);
                }
            }
            if (registeredInstance != null) {
                World.getInstance().setPosition(player, worldId, registeredInstance.getInstanceId(), player.getX(), player.getY(), player.getZ(),
                    player.getHeading());

                WorldMapInstance channel = player.getPosition().getWorldMapInstance();
                if (channel == null) { // may be null if player received Critical Error
                    moveToExitPoint(player);
                    return;
                }

                channel.getInstanceHandler().onPlayerLogin(player);
                return;
            }

            moveToExitPoint(player);
        }
    }

    public static void moveToExitPoint(Player player) {
        TeleportService.moveToInstanceExit(player, player.getWorldId(), player.getRace());
    }

    /**
     * @param worldId
     * @param instanceId
     *
     * @return
     */
    public static boolean isInstanceExist(int worldId, int instanceId) {
        return World.getInstance().getWorldMap(worldId).getWorldMapInstanceById(instanceId) != null;
    }

    /**
     * @param worldMapInstance
     */
    private static void startInstanceChecker(WorldMapInstance worldMapInstance) {
        int delay = 150000; // 2.5 minutes
        int period = 60000; // 1 minute
        worldMapInstance.setEmptyInstanceTask(ThreadPoolManager.getInstance()
            .scheduleAtFixedRate(new EmptyInstanceCheckerTask(worldMapInstance), delay, period));
    }
	
	public static void onOpenDoor(Player player, int doorId) {
        player.getPosition().getWorldMapInstance().getInstanceHandler().onOpenDoor(doorId);
    }

    public static void onLogOut(Player player) {
        WorldMapInstance channel = player.getPosition().getWorldMapInstance();
        if (channel != null) // may be null if player received Critical Error
        {
            channel.getInstanceHandler().onPlayerLogOut(player);
        }
    }

    public static void onEnterInstance(Player player) {
        WorldMapInstance channel = player.getPosition().getWorldMapInstance();
        if (channel == null) // may be null if player received Critical Error
        {
            return;
        }

        channel.getInstanceHandler().onEnterInstance(player);
        for (Item item : player.getInventory().getItems()) {
            if (item.getItemTemplate().getOwnershipWorld() != 0) {
                if (item.getItemTemplate().getOwnershipWorld() != player.getWorldId()) {
                    player.getInventory().decreaseByObjectId(item.getObjectId(), item.getItemCount());
                }
            }
        }
    }

    public static void onLeaveInstance(Player player) {
        WorldMapInstance channel = player.getPosition().getWorldMapInstance();
        if (channel == null) // may be null if player received Critical Error
        {
            return;
        }

        channel.getInstanceHandler().onLeaveInstance(player);
        for (Item item : player.getInventory().getItems()) {
            if (item.getItemTemplate().getOwnershipWorld() == player.getWorldId()) {
                player.getInventory().decreaseByObjectId(item.getObjectId(), item.getItemCount());
            }
        }
        AutoGroupService2.getInstance().onLeaveInstance(player);
    }

    public static void onEnterZone(Player player, ZoneInstance zone) {
        WorldMapInstance channel = player.getPosition().getWorldMapInstance();
        if (channel == null) // may be null if player received Critical Error
        {
            return;
        }

        channel.getInstanceHandler().onEnterZone(player, zone);
    }

    public static void onLeaveZone(Player player, ZoneInstance zone) {
        WorldMapInstance channel = player.getPosition().getWorldMapInstance();
        if (channel == null) // may be null if player received Critical Error
        {
            return;
        }

        channel.getInstanceHandler().onLeaveZone(player, zone);
    }

    public static boolean isAggro(int mapId) {
        return instanceAggro.contains(mapId);
    }

    public static int getInstanceRate(Player player, int mapId) {
        int instanceCooldownRate = player.havePermission(MembershipConfig.INSTANCES_COOLDOWN) && !instanceCoolDownFilter.contains(mapId) ? CustomConfig.INSTANCES_RATE
            : 1;
        if (instanceCoolDownFilter.contains(mapId)) {
            instanceCooldownRate = 1;
        }
        return instanceCooldownRate;
    }

    private static class EmptyInstanceCheckerTask implements Runnable {

        private final WorldMapInstance worldMapInstance;

        private EmptyInstanceCheckerTask(WorldMapInstance worldMapInstance) {
            this.worldMapInstance = worldMapInstance;
        }

        @Override
        public void run() {
            int instanceId = worldMapInstance.getInstanceId();
            int worldId = worldMapInstance.getMapId();
            WorldMap map = World.getInstance().getWorldMap(worldId);
            PlayerGroup registeredGroup = worldMapInstance.getRegisteredGroup();
            if (registeredGroup == null) {
                if (worldMapInstance.playersCount() == 0) {
                    map.removeWorldMapInstance(instanceId);
                    InstanceService.destroyInstance(worldMapInstance);
                    return;
                }
                Iterator<Player> playerIterator = worldMapInstance.playerIterator();
                int mapId = worldMapInstance.getMapId();
                while (playerIterator.hasNext()) {
                    Player player = playerIterator.next();
                    if ((player.isOnline()) && (player.getWorldId() == mapId)) {
                        return;
                    }
                }
                map.removeWorldMapInstance(instanceId);
                InstanceService.destroyInstance(worldMapInstance);
            } else if (registeredGroup.size() == 0) {
                map.removeWorldMapInstance(instanceId);
                InstanceService.destroyInstance(worldMapInstance);
            }
        }
    }
}
