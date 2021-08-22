/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.events.PlayerEnteredMap;
import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.templates.windstreams.Location2D;
import com.ne.gs.model.templates.windstreams.WindstreamTemplate;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_INSTANCE_COUNT_INFO;
import com.ne.gs.network.aion.serverpackets.SM_MOTION;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_WINDSTREAM_ANNOUNCE;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.SiegeService;
import com.ne.gs.services.WeatherService;
import com.ne.gs.spawnengine.InstanceRiftSpawnManager;
import com.ne.gs.spawnengine.RiftSpawnManager;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMapType;

/**
 * Client is saying that level[map] is ready.
 *
 * @author -Nemesiss-
 * @author Kwazar
 */
public class CM_LEVEL_READY extends AionClientPacket {

    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();

        if (activePlayer.isInInstance()) {
            sendPacket(new SM_INSTANCE_COUNT_INFO(activePlayer.getWorldId(), activePlayer.getInstanceId()));
        }
        sendPacket(new SM_PLAYER_INFO(activePlayer, false));
        activePlayer.getController().startProtectionActiveTask();
        sendPacket(new SM_MOTION(activePlayer.getObjectId(), activePlayer.getMotions().getActiveMotions()));

        WindstreamTemplate template = DataManager.WINDSTREAM_DATA.getStreamTemplate(activePlayer.getPosition().getMapId());
        Location2D location;
        if (template != null) {
            for (int i = 0; i < template.getLocations().getLocation().size(); i++) {
                location = template.getLocations().getLocation().get(i);
                sendPacket(new SM_WINDSTREAM_ANNOUNCE(location.getFlyPathType().getId(), template.getMapid(), location.getId(), location.getState()));
            }
        }

        /**
         * Spawn player into the world.
         */
        // If already spawned, despawn before spawning into the world
        if (activePlayer.isSpawned()) {
            World.getInstance().despawn(activePlayer);
        }
        World.getInstance().spawn(activePlayer);

        activePlayer.getController().refreshZoneImpl();

        if (activePlayer.isInSiegeWorld()) {
            SiegeService.getInstance().onEnterSiegeWorld(activePlayer);
        }
        activePlayer.getController().updateNearbyQuests();

        /**
         * Loading weather for the player's region
         */
        WeatherService.getInstance().loadWeather(activePlayer);

        QuestEngine.getInstance().onEnterWorld(new QuestEnv(null, activePlayer, 0, 0));

        activePlayer.getController().onEnterWorld();
        // zone channel message
        if (!WorldMapType.of(activePlayer.getWorldId()).isPersonal()) {
            sendPacket(new SM_SYSTEM_MESSAGE(1390122, activePlayer.getPosition().getInstanceId()));
        }
        RiftSpawnManager.sendRiftStatus(activePlayer);
        InstanceRiftSpawnManager.sendInstanceRiftStatus(activePlayer);

        activePlayer.getEffectController().updatePlayerEffectIcons();
        sendPacket(SM_CUBE_UPDATE.cubeSize(StorageType.CUBE, activePlayer));

        Pet pet = activePlayer.getPet();
        if (pet != null && !pet.isSpawned()) {
            World.getInstance().spawn(pet);
        }
        activePlayer.setPortAnimation(0);

        EventNotifier.GLOBAL.fire(PlayerEnteredMap.class, Tuple2.of(activePlayer, activePlayer.getWorldId()));
    }

}
