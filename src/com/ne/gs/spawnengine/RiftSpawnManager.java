/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.spawnengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.controllers.RiftController;
import com.ne.gs.controllers.effect.EffectController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnGroup2;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.NpcKnownList;

/**
 * @author ATracer, ginho1
 */
public final class RiftSpawnManager {

    private static final Logger log = LoggerFactory.getLogger(RiftSpawnManager.class);

    private static final ConcurrentLinkedQueue<Npc> rifts = new ConcurrentLinkedQueue<>();

    private static final int RIFT_RESPAWN_DELAY = 3600; // 1 hour
    private static final int RIFT_LIFETIME = 3500; // 1 hour

    private static final Map<String, SpawnTemplate> spawnGroups = new HashMap<>();

    public static void addRiftSpawnTemplate(SpawnGroup2 spawn) {
        if (spawn.hasPool()) {
            SpawnTemplate template = spawn.getSpawnTemplates().get(0);
            spawnGroups.put(template.getAnchor(), template);
        } else {
            for (SpawnTemplate template : spawn.getSpawnTemplates()) {
                spawnGroups.put(template.getAnchor(), template);
            }
        }
    }

    public static void spawnAll() {
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("Rift Manager");
                ArrayList<Integer> rifts = new ArrayList<>();
                int nbRift, rndRift;

                for (int i = 0; i < 5; i++) {
                    // Generate number of rift for each town
                    nbRift = getNbRift();

                    log.info("Spawning " + nbRift + " rifts for the map : " + getMapName(i));

                    for (int j = 0; j < nbRift; j++) {
                        rndRift = Rnd.get(i * 8, (i + 1) * 8 - 1);

                        // try to avoid duplicate
                        while (rifts.contains(rndRift)) {
                            rndRift = Rnd.get(i * 8, (i + 1) * 8 - 1);
                        }

                        // Save rift spawned
                        rifts.add(rndRift);

                        // Spawnrift
                        spawnRift(RiftEnum.values()[rndRift]);
                    }
                    rifts.clear();
                }
            }
        }, 0, RIFT_RESPAWN_DELAY * 1000);
    }

    /**
     * @return
     */
    private static int getNbRift() {
        double rnd = Rnd.get(0, 99);

		/*
         * 0 : 0% 1 : 0% 2 : 0% 3 : 0% 4 : 0% 5 : 0% 6 : 100%
		 */
        if (rnd <= 99) {
            return 6;
        } else if (rnd <= 0) {
            return 5;
        } else if (rnd <= 0) {
            return 4;
        } else if (rnd <= 0) {
            return 3;
        } else if (rnd <= 0) {
            return 2;
        } else if (rnd <= 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * @param mapId
     *
     * @return
     */
    private static String getMapName(int mapId) {
        switch (mapId) {
            case 0:
                return "INGGISON_GELKMAROS";
            case 1:
                return "ELTNEN";
            case 2:
                return "HEIRON_THEOBOMOS";
			case 3:
                return "MORHEIM";
            case 4:
                return "BELUSLAN_BRUSTHONIN";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * @param rift
     */
    private static void spawnRift(RiftEnum rift) {

        SpawnTemplate masterTemplate = spawnGroups.get(rift.getMaster());
        SpawnTemplate slaveTemplate = spawnGroups.get(rift.getSlave());

        if (masterTemplate == null || slaveTemplate == null) {
            return;
        }

        int instanceCount = World.getInstance().getWorldMap(masterTemplate.getWorldId()).getInstanceCount();

        if (slaveTemplate.hasPool()) {
            slaveTemplate = slaveTemplate.changeTemplate();
        }
        log.info("Spawning rift : " + rift.name());
        for (int i = 1; i <= instanceCount; i++) {
            Npc slave = spawnInstance(i, slaveTemplate, new RiftController(null, rift));
            spawnInstance(i, masterTemplate, new RiftController(slave, rift));
        }
    }

    private static Npc spawnInstance(int instanceIndex, SpawnTemplate st, RiftController riftController) {
        NpcTemplate masterObjectTemplate = DataManager.NPC_DATA.getNpcTemplate(700137);
        Npc npc = new Npc(IDFactory.getInstance().nextId(), riftController, st, masterObjectTemplate);

        npc.setKnownlist(new NpcKnownList(npc));
        npc.setEffectController(new EffectController(npc));

        World world = World.getInstance();
        world.storeObject(npc);
        world.setPosition(npc, st.getWorldId(), instanceIndex, st.getX(), st.getY(), st.getZ(), st.getHeading());
        world.spawn(npc);
        rifts.add(npc);

        scheduleDelete(npc);
        riftController.sendAnnounce();

        return npc;
    }

    /**
     * @param npc
     */
    private static void scheduleDelete(final Npc npc) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                npc.getSpawn().setUse(false);
                npc.getController().onDelete();
                rifts.remove(npc);
            }
        }, RIFT_LIFETIME * 1000);
    }

    public enum RiftEnum {
		INGGISON_AM("INGGISON_AM", "GELKMAROS_AS", 150, 20, 60, Race.ASMODIANS),
		INGGISON_BM("INGGISON_BM", "GELKMAROS_BS", 150, 20, 60, Race.ASMODIANS),
		INGGISON_CM("INGGISON_CM", "GELKMAROS_CS", 150, 20, 60, Race.ASMODIANS),
		INGGISON_DM("INGGISON_DM", "GELKMAROS_DS", 150, 20, 60, Race.ASMODIANS),
		GELKMAROS_AM("GELKMAROS_AM", "INGGISON_AS", 150, 20, 60, Race.ELYOS),
		GELKMAROS_BM("GELKMAROS_BM", "INGGISON_BS", 150, 20, 60, Race.ELYOS),
		GELKMAROS_CM("GELKMAROS_CM", "INGGISON_CS", 150, 20, 60, Race.ELYOS),
		GELKMAROS_DM("GELKMAROS_DM", "INGGISON_DS", 150, 20, 60, Race.ELYOS),
		
        ELTNEN_AM("ELTNEN_AM", "MORHEIM_AS", 80, 20, 45, Race.ASMODIANS),
        ELTNEN_BM("ELTNEN_BM", "MORHEIM_BS", 80, 20, 45, Race.ASMODIANS),
        ELTNEN_CM("ELTNEN_CM", "MORHEIM_CS", 80, 20, 45, Race.ASMODIANS),
        ELTNEN_DM("ELTNEN_DM", "MORHEIM_DS", 80, 20, 45, Race.ASMODIANS),
        ELTNEN_EM("ELTNEN_EM", "MORHEIM_ES", 80, 20, 45, Race.ASMODIANS),
        ELTNEN_FM("ELTNEN_FM", "MORHEIM_FS", 80, 20, 45, Race.ASMODIANS),
        ELTNEN_GM("ELTNEN_GM", "MORHEIM_GS", 80, 20, 45, Race.ASMODIANS),
        ELTNEN_HM("ELTNEN_HM", "MORHEIM_HS", 80, 20, 45, Race.ASMODIANS),

        HEIRON_AM("HEIRON_AM", "BELUSLAN_AS", 80, 20, 50, Race.ASMODIANS),
        HEIRON_BM("HEIRON_BM", "BELUSLAN_BS", 80, 20, 50, Race.ASMODIANS),
        HEIRON_CM("HEIRON_CM", "BELUSLAN_CS", 80, 20, 50, Race.ASMODIANS),
        HEIRON_DM("HEIRON_DM", "BELUSLAN_DS", 80, 20, 50, Race.ASMODIANS),
        HEIRON_EM("HEIRON_EM", "BELUSLAN_ES", 80, 20, 50, Race.ASMODIANS),
        HEIRON_FM("HEIRON_FM", "BELUSLAN_FS", 80, 20, CustomConfig.HEIRON_FM, Race.ASMODIANS),
        HEIRON_GM("HEIRON_GM", "BELUSLAN_GS", 80, 20, CustomConfig.HEIRON_GM, Race.ASMODIANS),
		THEOBOMOS_AM("THEOBOMOS_AM", "BRUSTHONIN_AS", 150, 20, 55, Race.ASMODIANS),
		
        MORHEIM_AM("MORHEIM_AM", "ELTNEN_AS", 80, 20, 45, Race.ELYOS),
        MORHEIM_BM("MORHEIM_BM", "ELTNEN_BS", 80, 20, 45, Race.ELYOS),
        MORHEIM_CM("MORHEIM_CM", "ELTNEN_CS", 80, 20, 45, Race.ELYOS),
        MORHEIM_DM("MORHEIM_DM", "ELTNEN_DS", 80, 20, 45, Race.ELYOS),
        MORHEIM_EM("MORHEIM_EM", "ELTNEN_ES", 80, 20, 45, Race.ELYOS),
        MORHEIM_FM("MORHEIM_FM", "ELTNEN_FS", 80, 20, 45, Race.ELYOS),
        MORHEIM_GM("MORHEIM_GM", "ELTNEN_GS", 80, 20, 45, Race.ELYOS),
		MORHEIM_HM("MORHEIM_HM", "ELTNEN_HS", 80, 20, 45, Race.ELYOS),

        BELUSLAN_AM("BELUSLAN_AM", "HEIRON_AS", 80, 20, 50, Race.ELYOS),
        BELUSLAN_BM("BELUSLAN_BM", "HEIRON_BS", 80, 20, 50, Race.ELYOS),
        BELUSLAN_CM("BELUSLAN_CM", "HEIRON_CS", 80, 20, 50, Race.ELYOS),
        BELUSLAN_DM("BELUSLAN_DM", "HEIRON_DS", 80, 20, 50, Race.ELYOS),
        BELUSLAN_EM("BELUSLAN_EM", "HEIRON_ES", 80, 20, 50, Race.ELYOS),
        BELUSLAN_FM("BELUSLAN_FM", "HEIRON_FS", 80, 20, CustomConfig.BELUSLAN_FM, Race.ELYOS),
        BELUSLAN_GM("BELUSLAN_GM", "HEIRON_GS", 80, 20, CustomConfig.BELUSLAN_GM, Race.ELYOS),
		BRUSTHONIN_AM("BRUSTHONIN_AM", "THEOBOMOS_AS", 150, 20, 55, Race.ELYOS);

        private final String master;
        private final String slave;
        private final int entries;
        private final int minLevel;
        private final int maxLevel;
        private final Race destination;

        private RiftEnum(String master, String slave, int entries, int minLevel, int maxLevel, Race destination) {
            this.master = master;
            this.slave = slave;
            this.entries = entries;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.destination = destination;
        }

        /**
         * @return the master
         */
        public String getMaster() {
            return master;
        }

        /**
         * @return the slave
         */
        public String getSlave() {
            return slave;
        }

        /**
         * @return the entries
         */
        public int getEntries() {
            return entries;
        }

        /**
         * @return the minLevel
         */
        public int getMinLevel() {
            return minLevel;
        }

        /**
         * @return the maxLevel
         */
        public int getMaxLevel() {
            return maxLevel;
        }

        /**
         * @return the destination
         */
        public Race getDestination() {
            return destination;
        }
    }

    /**
     * @param activePlayer
     */
    public static void sendRiftStatus(Player activePlayer) {
        for (Npc rift : rifts) {
            if (rift.getWorldId() == activePlayer.getWorldId()) {
                ((RiftController) rift.getController()).sendMessage(activePlayer);
            }
        }
    }
}
