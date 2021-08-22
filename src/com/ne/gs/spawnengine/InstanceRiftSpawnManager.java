/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.spawnengine;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author ginho1
 */
public final class InstanceRiftSpawnManager {

    private static final Logger log = LoggerFactory.getLogger(InstanceRiftSpawnManager.class);

    private static final ConcurrentLinkedQueue<VisibleObject> rifts = new ConcurrentLinkedQueue<>();

    private static final int RIFT_RESPAWN_DELAY = 3600; // 1 hour
    private static final int RIFT_LIFETIME = 3500; // 1 hour

    public static void spawnAll() {

        ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {

                for (RiftEnum rift : RiftEnum.values()) {
                    if (Rnd.get(1, 100) > 30) {
                        continue;
                    }

                    spawnInstanceRift(rift);
                }
            }
        }, 0, RIFT_RESPAWN_DELAY * 1000);
    }

    private static void spawnInstanceRift(RiftEnum rift) {
        log.info("Spawning Instance Rift: " + rift.name());

        SpawnTemplate spawn = SpawnEngine.addNewSpawn(rift.getWorldId(), rift.getNpcId(), rift.getX(), rift.getY(), rift.getZ(), (byte) 0, 0);

        if (rift.getStaticId() > 0) {
            spawn.setStaticId(rift.getStaticId());
        }

        VisibleObject visibleObject = SpawnEngine.spawnObject(spawn, 1);

        rifts.add(visibleObject);

        scheduleDelete(visibleObject);
        sendAnnounce(visibleObject);
    }

    private static void scheduleDelete(final VisibleObject visObj) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (visObj != null && visObj.isSpawned()) {
                    visObj.getController().delete();
                    rifts.remove(visObj);
                }
            }
        }, RIFT_LIFETIME * 1000);
    }

    public static void sendInstanceRiftStatus(Player activePlayer) {
        for (VisibleObject visObj : rifts) {
            if (visObj.getWorldId() == activePlayer.getWorldId()) {
                sendMessage(activePlayer, visObj.getObjectTemplate().getTemplateId());
            }
        }
    }

    public static void sendAnnounce(final VisibleObject visObj) {
        if (visObj.isSpawned()) {
            WorldMapInstance worldInstance = visObj.getPosition().getMapRegion().getParent();

            worldInstance.doOnAllPlayers(new Visitor<Player>() {
                @Override
                public void visit(Player player) {
                    if (player.isSpawned()) {
                        sendMessage(player, visObj.getObjectTemplate().getTemplateId());
                    }
                }
            });
        }
    }

    public static void sendMessage(Player player, int npc_id) {
        switch (npc_id) {
            case 700564:
                player.sendPck(new SM_SYSTEM_MESSAGE(1400276));
                break;
            case 700565:
                player.sendPck(new SM_SYSTEM_MESSAGE(1400275));
        }
    }

    public enum RiftEnum {
        DraupnirCave(700564, 1617, Race.ELYOS, 210040000, 2528.6621F, 2680.8821F, 155.05F),
        IndratuFortress(700565, 0, Race.ASMODIANS, 220040000, 1466.8792F,
            1947.9192F, 588.06555F);

        private final int npc_id;
        private final int static_id;
        private final Race race;
        private final int worldId;
        private final float x;
        private final float y;
        private final float z;

        private RiftEnum(int npc_id, int static_id, Race race, int worldId, float x, float y, float z) {
            this.npc_id = npc_id;
            this.static_id = static_id;
            this.race = race;
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getNpcId() {
            return npc_id;
        }

        public int getStaticId() {
            return static_id;
        }

        public Race getRace() {
            return race;
        }

        public int getWorldId() {
            return worldId;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }
    }
}
