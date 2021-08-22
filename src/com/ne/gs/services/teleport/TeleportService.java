/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.teleport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.math.AionPos;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.PlayerInitialData.LocationData;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.Race;
import com.ne.gs.model.TeleportAnimation;
import com.ne.gs.model.TribeClass;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.events.PlayerLeftMap;
import com.ne.gs.model.events.PlayerTeleported;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.BindPointPosition;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.templates.flypath.FlyPathEntry;
import com.ne.gs.model.templates.portal.InstanceExit;
import com.ne.gs.model.templates.portal.PortalLoc;
import com.ne.gs.model.templates.portal.PortalPath;
import com.ne.gs.model.templates.portal.PortalScroll;
import com.ne.gs.model.templates.spawns.SpawnSearchResult;
import com.ne.gs.model.templates.spawns.SpawnSpotTemplate;
import com.ne.gs.model.templates.teleport.TelelocationTemplate;
import com.ne.gs.model.templates.teleport.TeleportLocation;
import com.ne.gs.model.templates.teleport.TeleportType;
import com.ne.gs.model.templates.teleport.TeleporterTemplate;
import com.ne.gs.model.templates.world.WorldMapTemplate;
import com.ne.gs.network.aion.serverpackets.*;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.DuelService;
import com.ne.gs.services.PrivateStoreService;
import com.ne.gs.services.SiegeService;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.services.player.PlayerReviveService;
import com.ne.gs.services.trade.PricesService;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.WorldMapType;
import com.ne.gs.world.WorldPosition;

public final class TeleportService {

    private static final Logger log = LoggerFactory.getLogger(TeleportService.class);

    public static void teleport(TeleporterTemplate template, int locId, Player player, Npc npc, TeleportAnimation animation) {
        TribeClass tribe = npc.getTribe();
        Race race = player.getRace();
        if (tribe.equals(TribeClass.FIELD_OBJECT_LIGHT) && race.equals(Race.ASMODIANS) || tribe.equals(TribeClass.FIELD_OBJECT_DARK) && race
            .equals(Race.ELYOS)) {
            return;
        }

        if (template.getTeleLocIdData() == null) {
            log.info(String.format("Missing locId for this teleporter at teleporter_templates.xml with locId: %d", locId));
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE);
            if (player.isGM()) {
                player.sendMsg("Missing locId for this teleporter at teleporter_templates.xml with locId: " + locId);
            }
            return;
        }

        TeleportLocation location = template.getTeleLocIdData().getTeleportLocation(locId);
        if (location == null) {
            log.info(String.format("Missing locId for this teleporter at teleporter_templates.xml with locId: %d", locId));
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE);
            if (player.isGM()) {
                player.sendMsg("Missing locId for this teleporter at teleporter_templates.xml with locId: " + locId);
            }
            return;
        }

        TelelocationTemplate locationTemplate = DataManager.TELELOCATION_DATA.getTelelocationTemplate(locId);
        if (locationTemplate == null) {
            log.info(String.format("Missing info at teleport_location.xml with locId: %d", locId));
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE);
            if (player.isGM()) {
                player.sendMsg("Missing info at teleport_location.xml with locId: " + locId);
            }
            return;
        }

        if (location.getRequiredQuest() > 0) {
            QuestState qs = player.getQuestStateList().getQuestState(location.getRequiredQuest());
            if (qs == null || qs.getStatus() != QuestStatus.COMPLETE) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NEED_FINISH_QUEST);
                return;
            }
        }
        
        if (player.getLifeStats().isAlreadyDead()) {
            PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
            PlayerReviveService.revive(player, 20, 20, true, 0);
        } 

        int id = SiegeService.getInstance().getFortressId(locId);
        if (id > 0 && !SiegeService.getInstance().getFortress(id).isCanTeleport(player)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE);
            player.sendMsg("Teleporter is dead");
            return;
        }

        if (!checkKinahForTransportation(location, player)) {
            return;
        }

        if (location.getType() == TeleportType.FLIGHT) {
            if (SecurityConfig.ENABLE_FLYPATH_VALIDATOR) {
                FlyPathEntry flypath = DataManager.FLY_PATH.getPathTemplate((byte) location.getLocId());
                if (flypath == null) {
                    AuditLogger.info(player, "Try to use null flyPath #" + location.getLocId());
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE);
                    return;
                }

                double dist = MathUtil.getDistance(player, flypath.getStartX(), flypath.getStartY(), flypath.getStartZ());
                if (dist > 7) {
                    AuditLogger.info(player, "Try to use flyPath #" + location.getLocId() + " but hes too far " + dist);

                    player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE);
                    return;
                }

                if (player.getWorldId() != flypath.getStartWorldId()) {
                    AuditLogger.info(player, "Try to use flyPath #" + location.getLocId() + " from not native start world " + player
                        .getWorldId()
                        + ". expected " + flypath.getStartWorldId());

                    player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE);
                    return;
                }

                player.setCurrentFlypath(flypath);
            }
            player.unsetPlayerMode(PlayerMode.RIDE);
            player.setState(CreatureState.FLIGHT_TELEPORT);
            player.unsetState(CreatureState.ACTIVE);
            player.setFlightTeleportId(location.getTeleportId());
            PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, location.getTeleportId(), 0), true);
        } else {
            int instanceId = 1;
            int mapId = locationTemplate.getMapId();
            if (player.getWorldId() == mapId) {
                instanceId = player.getInstanceId();
            }
            sendLoc(player, mapId, instanceId, locationTemplate.getX(), locationTemplate.getY(), locationTemplate.getZ(), (byte) locationTemplate
                .getHeading(),
                animation);
        }
    }

    private static boolean checkKinahForTransportation(TeleportLocation location, Player player) {
        Storage inventory = player.getInventory();

        int basePrice = location.getPrice();

        long transportationPrice = PricesService.getPriceForService(basePrice, player.getRace());

        if (player.getController().isHiPassInEffect()) {
            transportationPrice = 1;
        }
        if (!inventory.tryDecreaseKinah(transportationPrice)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(transportationPrice));
            return false;
        }
        return true;
    }

    private static void sendLoc(final Player player, final int mapId, final int instanceId, final float x, final float y, final float z, final int h,
                                final TeleportAnimation animation) {
        boolean isInstance = DataManager.WORLD_MAPS_DATA.getTemplate(mapId).isInstance();
        player.sendPck(new SM_TELEPORT_LOC(isInstance, instanceId, mapId, x, y, z, h, animation.getStartAnimationId()));
        player.unsetPlayerMode(PlayerMode.RIDE);
        player.unsetState(CreatureState.ENTERED_WINDS);
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (player.getLifeStats().isAlreadyDead() || !player.isSpawned()) {
                    return;
                }
                TeleportService.changePosition(player, mapId, instanceId, x, y, z, h, animation);
            }
        }, 2200);
    }

    public static boolean teleportBeam(Player player, int worldId, float x, float y, float z) {
        return teleportTo(player, worldId, x, y, z, player.getHeading(), TeleportAnimation.BEAM_ANIMATION);
    }

    public static boolean teleportBeam(Player player, int worldId, int channelId, float x, float y, float z) {
        return teleportTo(player, worldId, channelId, x, y, z, player.getHeading(), TeleportAnimation.BEAM_ANIMATION);
    }

    public static boolean teleportBeam(Player player, AionPos pos) {
        return teleportBeam(player, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean teleportBeam(Player player, int channelId, AionPos pos) {
        return teleportTo(player, pos.getMapId(), channelId, pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean teleportTo(Player player, int worldId, float x, float y, float z) {
        return teleportTo(player, worldId, x, y, z, player.getHeading());
    }

    public static boolean teleportTo(Player player, int worldId, float x, float y, float z, int h) {
        int instanceId = 1;
        if (player.getWorldId() == worldId) {
            instanceId = player.getInstanceId();
        }
        return teleportTo(player, worldId, instanceId, x, y, z, h, TeleportAnimation.NO_ANIMATION);
    }

    public static boolean teleportTo(Player player, int worldId, float x, float y, float z, int h,
                                     TeleportAnimation animation) {
        int instanceId = 1;
        if (player.getWorldId() == worldId) {
            instanceId = player.getInstanceId();
        }
        return teleportTo(player, worldId, instanceId, x, y, z, h, animation);
    }

    public static boolean teleportTo(Player player, int worldId, int instanceId, float x, float y, float z, int h) {
        return teleportTo(player, worldId, instanceId, x, y, z, h, TeleportAnimation.NO_ANIMATION);
    }

    public static boolean teleportTo(Player player, int worldId, int instanceId, float x, float y, float z) {
        return teleportTo(player, worldId, instanceId, x, y, z, player.getHeading(), TeleportAnimation.NO_ANIMATION);
    }

    public static boolean teleportTo(Player player, int worldId, int instanceId, float x, float y, float z,
                                     int heading, TeleportAnimation animation) {
        if (player.getLifeStats().isAlreadyDead()) {
            return false;
        }

        EventNotifier.GLOBAL.fire(PlayerTeleported.class, player);
        player.getNotifier().fire(PlayerTeleported.class, player);

        if (player.getWorldId() != worldId) {
            player.getController().onLeaveWorld();
            EventNotifier.GLOBAL.fire(PlayerLeftMap.class, Tuple2.of(player, player.getWorldId()));
            player.getNotifier().fire(PlayerLeftMap.class, Tuple2.of(player, player.getWorldId()));
        }

        if (animation.isNoAnimation()) {
            player.unsetPlayerMode(PlayerMode.RIDE);
            player.unsetState(CreatureState.ENTERED_WINDS);
            changePosition(player, worldId, instanceId, x, y, z, heading, animation);
        } else {
            sendLoc(player, worldId, instanceId, x, y, z, heading, animation);
        }
        return true;
    }

    private static void changePosition(Player player, int worldId, int instanceId, float x, float y, float z,
                                       int heading, TeleportAnimation animation) {
        if (player.hasStore()) {
            PrivateStoreService.closePrivateStore(player);
        }
        player.getFlyController().endFly(true);

        World.getInstance().despawn(player);

        int currentWorldId = player.getWorldId();
        boolean isInstance = DataManager.WORLD_MAPS_DATA.getTemplate(worldId).isInstance();
        World.getInstance().setPosition(player, worldId, instanceId, x, y, z, heading);

        Pet pet = player.getPet();
        if (pet != null) {
            World.getInstance().setPosition(pet, worldId, instanceId, x, y, z, heading);
        }

        player.setPortAnimation(animation.getEndAnimationId());
        player.getController().startProtectionActiveTask();
        if (currentWorldId == worldId) {
            player.sendPck(new SM_PLAYER_INFO(player, false));
            player.sendPck(new SM_STATS_INFO(player));
            player.sendPck(new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
            World.getInstance().spawn(player);
            player.getEffectController().updatePlayerEffectIcons();
            player.getController().updateZone();

            if (pet != null) {
                World.getInstance().spawn(pet);
            }
            player.setPortAnimation(0); //может быть баг с таргетом на ивенте
        } else {
            player.sendPck(new SM_CHANNEL_INFO(player.getPosition()));
            player.sendPck(new SM_PLAYER_SPAWN(player));
        }
        if (player.isLegionMember()) {
            PacketSendUtility.broadcastPacketToLegion(player.getLegion(), new SM_LEGION_UPDATE_MEMBER(player, 0, ""));
        }
        sendWorldSwitchMessage(player, currentWorldId, worldId, isInstance);
    }

    private static void sendWorldSwitchMessage(Player player, int oldWorld, int newWorld, boolean enteredInstance) {
        if (enteredInstance && oldWorld != newWorld && !WorldMapType.of(newWorld).isPersonal()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_DUNGEON_OPENED_FOR_SELF(newWorld));
        }
    }

    /**
     * @param player
     * @param targetObjectId
     */
    public static void showMap(Player player, int targetObjectId, int npcId) {
        if (player.isInFlyingState()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_AIRPORT_WHEN_FLYING);
            return;
        }

        Npc object = (Npc) World.getInstance().findVisibleObject(targetObjectId);
        if (player.isEnemy(object)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_WRONG_NPC);
            // message
            return;
        }

        player.sendPck(new SM_TELEPORT_MAP(player, targetObjectId, getTeleporterTemplate(npcId)));
    }

    public static TeleporterTemplate getTeleporterTemplate(int npcId) {
        return DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(npcId);
    }

    public static void moveToKiskLocation(Player player, WorldPosition kisk) {
        int mapId = kisk.getMapId();
        float x = kisk.getX();
        float y = kisk.getY();
        float z = kisk.getZ();
        int heading = kisk.getH();

        teleportTo(player, mapId, x, y, z, heading);
    }

    public static void teleportToPrison(Player player) {
        if (player.getRace() == Race.ELYOS) {
            teleportTo(player, WorldMapType.DE_PRISON.getId(), 275.0F, 239.0F, 49.0F);
        } else if (player.getRace() == Race.ASMODIANS) {
            teleportTo(player, WorldMapType.DF_PRISON.getId(), 275.0F, 239.0F, 49.0F);
        }
    }

    public static void teleportToNpc(Player player, int npcId) {
        int worldId = player.getWorldId();
        SpawnSearchResult searchResult = DataManager.SPAWNS_DATA2.getFirstSpawnByNpcId(worldId, npcId);

        if (searchResult == null) {
            log.warn("No npc spawn found for : " + npcId);
            return;
        }

        SpawnSpotTemplate spot = searchResult.getSpot();
        WorldMapTemplate worldTemplate = DataManager.WORLD_MAPS_DATA.getTemplate(searchResult.getWorldId());
        WorldMapInstance newInstance = null;

        if (worldTemplate.isInstance()) {
            newInstance = InstanceService.getNextAvailableInstance(searchResult.getWorldId());
        }

        if (newInstance != null) {
            InstanceService.registerPlayerWithInstance(newInstance, player);
            teleportTo(player, searchResult.getWorldId(), newInstance.getInstanceId(), spot.getX(), spot.getY(), spot.getZ());
        } else {
            teleportTo(player, searchResult.getWorldId(), spot.getX(), spot.getY(), spot.getZ());
        }
    }

    public static void sendSetBindPoint(Player player) {
        int worldId;
        float x, y, z;
        if (player.getBindPoint() != null) {
            BindPointPosition bplist = player.getBindPoint();
            worldId = bplist.getMapId();
            x = bplist.getX();
            y = bplist.getY();
            z = bplist.getZ();
        } else {
            LocationData locationData = DataManager.PLAYER_INITIAL_DATA.getSpawnLocation(player.getRace());
            worldId = locationData.getMapId();
            x = locationData.getX();
            y = locationData.getY();
            z = locationData.getZ();
        }
        player.sendPck(new SM_SET_BIND_POINT(worldId, x, y, z, player));
    }

    public static void moveToBindLocation(Player player, boolean useTeleport) {
        moveToBindLocation(player, useTeleport, 0);
    }

    public static void moveToBindLocation(Player player, boolean useTeleport, int delay) {
        int h = 0;
        int worldId;
        float x;
        float y;
        float z;
        if (player.getBindPoint() != null) {
            BindPointPosition bplist = player.getBindPoint();
            worldId = bplist.getMapId();
            x = bplist.getX();
            y = bplist.getY();
            z = bplist.getZ();
            h = bplist.getHeading();
        } else {
            LocationData locationData = DataManager.PLAYER_INITIAL_DATA.getSpawnLocation(player.getRace());
            worldId = locationData.getMapId();
            x = locationData.getX();
            y = locationData.getY();
            z = locationData.getZ();
        }

        InstanceService.onLeaveInstance(player);

        if (useTeleport) {
            teleportTo(player, worldId, x, y, z, h);
        } else {
            World.getInstance().setPosition(player, worldId, 1, x, y, z, h);
        }
    }

    public static boolean moveToTargetWithDistance(VisibleObject object, Player player, int direction, int distance) {
        double radian = Math.toRadians(object.getHeading() * 3);
        float x0 = object.getX();
        float y0 = object.getY();
        float x1 = (float) (Math.cos(Math.PI * direction + radian) * distance);
        float y1 = (float) (Math.sin(Math.PI * direction + radian) * distance);
        return teleportTo(player, object.getWorldId(), x0 + x1, y0 + y1, object.getZ());
    }

    public static void moveToInstanceExit(Player player, int worldId, Race race) {
        InstanceExit instanceExit = getInstanceExit(worldId, race);
        if (instanceExit == null) {
            log.warn("No instance exit found for race: " + race + " " + worldId);
            moveToBindLocation(player, true);
            return;
        }
        if (InstanceService.isInstanceExist(instanceExit.getExitWorld(), 1)) {
            teleportTo(player, instanceExit.getExitWorld(), instanceExit.getX(), instanceExit.getY(), instanceExit.getZ(), instanceExit
                .getH());
        } else {
            moveToBindLocation(player, true);
        }
    }

    public static InstanceExit getInstanceExit(int worldId, Race race) {
        return DataManager.INSTANCE_EXIT_DATA.getInstanceExit(worldId, race);
    }

    public static void useTeleportScroll(Player player, String portalName, int worldId) {
        PortalScroll template = DataManager.PORTAL2_DATA.getPortalScroll(portalName);
        if (template == null) {
            log.warn("No portal template found for : " + portalName + " " + worldId);
            return;
        }

        Race playerRace = player.getRace();
        PortalPath portalPath = template.getPortalPath();
        if (portalPath == null) {
            log.warn("No portal scroll for " + playerRace + " on " + portalName + " " + worldId);
            return;
        }
        PortalLoc loc = DataManager.PORTAL_LOC_DATA.getPortalLoc(portalPath.getLocId());
        if (loc == null) {
            log.warn("No portal loc for locId" + portalPath.getLocId());
            return;
        }
        teleportTo(player, worldId, loc.getX(), loc.getY(), loc.getZ());
    }

    public static void changeChannel(Player player, int channel) {
        World.getInstance().despawn(player);
        World.getInstance()
             .setPosition(player, player.getWorldId(), channel + 1, player.getX(), player.getY(), player.getZ(), player.getHeading());

        player.getController().startProtectionActiveTask();
        player.sendPck(new SM_CHANNEL_INFO(player.getPosition()));
        player.sendPck(new SM_PLAYER_SPAWN(player));
    }
}
