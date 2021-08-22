/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ne.gs.database.GDB;
import com.ne.gs.model.gameobjects.Kisk;
import javolution.util.FastMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.DateUtil;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.services.CronService;
import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.AdvCustomConfig;
import com.ne.gs.configs.main.SiegeConfig;
import com.ne.gs.configs.shedule.SiegeSchedule;
import com.ne.gs.configs.shedule.SiegeSchedule.Fortress;
import com.ne.gs.configs.shedule.SiegeSchedule.Source;
import com.ne.gs.database.dao.SiegeDAO;
import com.ne.gs.database.dao.TaskFromDBDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.PlayerInitialData;
import com.ne.gs.model.DescId;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.BindPointPosition;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.siege.*;
import com.ne.gs.model.tasks.TaskFromDB;
import com.ne.gs.model.templates.npc.AbyssNpcType;
import com.ne.gs.model.templates.spawns.SpawnGroup2;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.*;
import com.ne.gs.services.siegeservice.*;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.WorldType;
import com.ne.gs.world.knownlist.Visitor;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author SoulKeeper
 */
public class SiegeService {

    /**
     * Just a logger
     */
    private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
    /**
     * Balaur protector spawn schedule.
     */
    private static final String RACE_PROTECTOR_SPAWN_SCHEDULE = SiegeConfig.RACE_PROTECTOR_SPAWN_SCHEDULE;
    /**
     * Balaurea race protector spawn schedule.
     */
    private static final String BERSERKER_SUNAYAKA_SPAWN_SCHEDULE = SiegeConfig.BERSERKER_SUNAYAKA_SPAWN_SCHEDULE;
    /**
     * Balaurea race protector spawn schedule.
     */
    private static final int GOVERNOR_SUNAYAKA_RESPAWN_FROM = SiegeConfig.GOVERNOR_SUNAYAKA_RESPAWN_FROM;
    /**
     * Balaurea race protector spawn schedule.
     */
    private static final int GOVERNOR_SUNAYAKA_RESPAWN_TO = SiegeConfig.GOVERNOR_SUNAYAKA_RESPAWN_TO;
    /**
     * We should broadcast fortress status every hour Actually only influence
     * packet must be sent, but that doesn't matter
     */
    private static final String SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE = "0 0 * ? * *";
    /**
     * MOLTENUS spawn schedule.
     */
	private static final String MOLTENUS_SPAWN_SCHEDULE = SiegeConfig.MOLTENUS_SPAWN_SCHEDULE;
    /**
     * Singleton that is loaded on the class initialization. Guys, we really do
     * not SingletonHolder classes
     */
    private static final SiegeService instance = new SiegeService();
    /**
     * Map that holds fortressId to Siege. We can easily know what fortresses is
     * under siege ATM :)
     */
    private final Map<Integer, Siege<?>> activeSieges = new FastMap<Integer, Siege<?>>().shared();
    /**
     * Object that holds siege schedule.<br>
     * And maybe other useful information (in future).
     */
    private SiegeSchedule siegeSchedule;
    /**
     * Tiamaranta's eye infiltration route status cl - Western Tiamaranta's Eye
     * Entrance (Center left) cr - Eastern Tiamaranta's Eye Entrance (Center
     * right) tl - Eye Abyss Gate Elyos (Top left) tr - Eye Abyss Gate Asmodians
     * (Top rigft)
     */
    private boolean cl;
    private boolean cr;
    private boolean tl;
    private boolean tr;
    private final FastMap<Integer, VisibleObject> tiamarantaPortals = new FastMap<>();
    private final FastMap<Integer, VisibleObject> tiamarantaEyeBoss = new FastMap<>();
    private Map<Integer, ArtifactLocation> artifacts;
    private Map<Integer, FortressLocation> fortresses;
    private Map<Integer, OutpostLocation> outposts;
    private Map<Integer, SourceLocation> sources;
    private Map<Integer, SiegeLocation> locations;
	private FastMap<Integer, VisibleObject> moltenusAbyssBoss = new FastMap<Integer, VisibleObject>();

    private volatile VisibleObject isSpawnGovernorSunayaka218553 = null;

    public static class SiegeBoss {

        public static final int BERSERKER_SUNAYAKA_219359 = 219359;
        public static final int GOVERNOR_SUNAYAKA_218553 = 218553;
        public static final int LIMB_RENDER_283074 = 283074;
        public static final int LIMB_RENDER_283076 = 283076;
    }

    private SiegeService() {
        if (SiegeConfig.ALL_TIME_AVAILABLE_PORTALS) {
            spawnTiamarantaPortels(true, true, true, true);
            broadcast(new SM_RIFT_ANNOUNCE(true, true, true, true), null);
        }
    }

    public static SiegeService getInstance() {
        return instance;
    }

    public void initSiegeLocations() {
        if (SiegeConfig.SIEGE_ENABLED) {
            log.info("Initializing sieges...");

            if (siegeSchedule != null) {
                log.error("SiegeService should not be initialized two times!");
                return;
            }

            // initialize current siege locations
            artifacts = DataManager.SIEGE_LOCATION_DATA.getArtifacts();
            fortresses = DataManager.SIEGE_LOCATION_DATA.getFortress();
            outposts = DataManager.SIEGE_LOCATION_DATA.getOutpost();
            sources = DataManager.SIEGE_LOCATION_DATA.getSource();
            locations = DataManager.SIEGE_LOCATION_DATA.getSiegeLocations();
            GDB.get(SiegeDAO.class).loadSiegeLocations(locations);
        } else {
            artifacts = Collections.emptyMap();
            fortresses = Collections.emptyMap();
            outposts = Collections.emptyMap();
            sources = Collections.emptyMap();
            locations = Collections.emptyMap();
            log.info("Sieges are disabled in config.");
        }
    }

    @SuppressWarnings("deprecation")
    public void initSieges() {
        if (!SiegeConfig.SIEGE_ENABLED) {
            return;
        }

        // despawn all NPCs spawned by spawn engine.
        // Siege spawns should be controlled by siege service
        for (Integer i : getSiegeLocations().keySet()) {
            deSpawnNpcs(i);
        }

        // spawn fortress common npcs
        for (FortressLocation f : getFortresses().values()) {
            spawnNpcs(f.getLocationId(), f.getRace(), SiegeModType.PEACE);
        }

        // spawn fortress common npcs
        for (SourceLocation s : getSources().values()) {
            spawnNpcs(s.getLocationId(), s.getRace(), SiegeModType.PEACE);
        }

        // spawn outpost protectors...
        for (OutpostLocation o : getOutposts().values()) {
            if (SiegeRace.BALAUR != o.getRace() && o.getLocationRace() != o.getRace()) {
                spawnNpcs(o.getLocationId(), o.getRace(), SiegeModType.PEACE);
            }
        }

        // spawn artifacts
        for (ArtifactLocation a : getStandaloneArtifacts().values()) {
            spawnNpcs(a.getLocationId(), a.getRace(), SiegeModType.PEACE);
        }

        // initialize siege schedule
        siegeSchedule = SiegeSchedule.load();

        // Schedule fortresses sieges protector spawn
        for (Fortress f : siegeSchedule.getFortressesList()) {
            for (String siegeTime : f.getSiegeTimes()) {
                CronService.getInstance().schedule(new SiegeStartRunnable(f.getId()), siegeTime);
                log.debug("Scheduled siege of fortressID " + f.getId() + " based on cron expression: " + siegeTime);
            }
        }

        // Schedule sources sieges preparation start
        for (Source s : siegeSchedule.getSourcesList()) {
            for (String siegeTime : s.getSiegeTimes()) {
                CronService.getInstance().schedule(new SiegeStartRunnable(s.getId()), siegeTime);
                log.debug("Scheduled siege of sourceID " + s.getId() + " based on cron expression: " + siegeTime);
            }
        }

        // Sync Tiamaranta's eye infiltration route status
        updateTiamarantaRiftsStatus(false, true);


        //Abyss Moltenus start...
        CronService.getInstance().schedule(new Runnable() {
		@Override
		public void run() {
				if (moltenusAbyssBoss.containsKey(251045) && moltenusAbyssBoss.get(251045).isSpawned())
					log.warn("Moltenus was already spawned...");
				else {
					int randomPos = Rnd.get(1, 3);
					switch (randomPos) {
						case 1:
							moltenusAbyssBoss.put(251045, SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 2464.9199f, 1689f, 2882.221f, (byte) 0), 1));
							break;
						case 2:
							moltenusAbyssBoss.put(251045, SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 2263.4812f, 2587.1633f, 2879.5447f, (byte) 0), 1));
							break;
						case 3:
							moltenusAbyssBoss.put(251045, SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 1692.96f, 1809.04f, 2886.027f, (byte) 0), 1));
							break;
					}
					log.info("Moltenus spawned in the Abyss");
					World.getInstance().doOnAllPlayers(new Visitor<Player>() {
						@Override
						public void visit(Player player) {
							PacketSendUtility.sendYellowMessageOnCenter(player, "В Бездне появился Гневный циклоп Менотиос.");
						}

					});
					//Moltenus despawned after 1 hr if not killed
					ThreadPoolManager.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							for (VisibleObject vo : moltenusAbyssBoss.values()) {
								if (vo != null) {
									Npc npc = (Npc) vo;
									if (!npc.getLifeStats().isAlreadyDead()) {
										npc.getController().onDelete();
									}
								}
								moltenusAbyssBoss.clear();
								log.info("Moltenus dissapeared");
								World.getInstance().doOnAllPlayers(new Visitor<Player>() {
									@Override
									public void visit(Player player) {
										PacketSendUtility.sendYellowMessageOnCenter(player, "Гневный циклоп Менотиос исчез.");
									}

								});
							}
						}

					}, 3600 * 1000);
				}
			}

		}, MOLTENUS_SPAWN_SCHEDULE);


        // Outpost siege start... Why it's called balaur?
        CronService.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                // В кроне и так есть задание дня.
//                Calendar calendar = Calendar.getInstance();
//                if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
//                    return;
//                }
                if ((cl) && (cr)) {
                    if (tiamarantaEyeBoss.containsKey(SiegeBoss.BERSERKER_SUNAYAKA_219359) && tiamarantaEyeBoss.get(SiegeBoss.BERSERKER_SUNAYAKA_219359)
                            .isSpawned()) {
                        SiegeService.log.warn("Berserker Sunayaka was already spawned...");
                        return;
                    }
                    if (isSpawnGovernorSunayaka218553 != null) {
                        SiegeService.log.info("Governor Sunayaka was already spawnes. Delete him and spawn Berserker Sunayaka.");
                        Npc npc = (Npc) isSpawnGovernorSunayaka218553;
                        if (!npc.getLifeStats().isAlreadyDead()) {
                            npc.getController().onDelete();
                        }
                    }
                    tiamarantaEyeBoss.put(SiegeBoss.BERSERKER_SUNAYAKA_219359,
                            SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600040000, SiegeBoss.BERSERKER_SUNAYAKA_219359, 759.09583F, 765.68158F, 1226.5004F, (byte) 0), 1));

                    tiamarantaEyeBoss.put(SiegeBoss.LIMB_RENDER_283074,
                            SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600040000, SiegeBoss.LIMB_RENDER_283074, 754.84625F, 819.98828F, 1223.957F, (byte) 0), 1));

                    tiamarantaEyeBoss.put(SiegeBoss.LIMB_RENDER_283076,
                            SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(600040000, SiegeBoss.LIMB_RENDER_283076, 754.90234F, 712.99042F, 1223.957F, (byte) 0), 1));

                    // Jenelli 27.05.2013 в текущей версии не убираем моба через час.
//                    if (bossId == 219359) {
//                        ThreadPoolManager.getInstance().schedule(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                for (VisibleObject vo : tiamarantaEyeBoss.values()) {
//                                    if (vo != null) {
//                                        Npc npc = (Npc) vo;
//                                        if (!npc.getLifeStats().isAlreadyDead()) {
//                                            npc.getController().onDelete();
//                                        }
//                                    }
//                                    tiamarantaEyeBoss.clear();
//                                }
//                            }
//                        }, 3600000);
//                    }
                }
            }
        }, BERSERKER_SUNAYAKA_SPAWN_SCHEDULE);
        CronService.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                // spawn outpost protectors...
                for (OutpostLocation o : getOutposts().values()) {
                    if (o.isSiegeAllowed()) {
                        startSiege(o.getLocationId());
                    }
                }
            }
        }, RACE_PROTECTOR_SPAWN_SCHEDULE);

        // Start siege of artifacts
        for (ArtifactLocation artifact : artifacts.values()) {
            if (artifact.isStandAlone()) {
                log.debug("Starting siege of artifact #" + artifact.getLocationId());
                startSiege(artifact.getLocationId());
            } else {
                log.debug("Artifact #" + artifact.getLocationId() + " siege was not started, it belongs to fortress");
            }
        }

        // We should set valid next state for fortress on startup
        // no need to broadcast state here, no players @ server ATM
        updateFortressNextState();

        // Schedule siege status broadcast (every hour)
        CronService.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                updateFortressNextState();
                World.getInstance().doOnAllPlayers(new Visitor<Player>() {

                    @Override
                    public void visit(Player player) {
                        for (FortressLocation fortress : getFortresses().values()) {
                            player.sendPck(new SM_FORTRESS_INFO(fortress.getLocationId(), false));
                        }
                        player.sendPck(new SM_FORTRESS_STATUS());

                        for (FortressLocation fortress : getFortresses().values()) {
                            player.sendPck(new SM_FORTRESS_INFO(fortress.getLocationId(), true));
                        }
                    }
                });
            }
        }, SIEGE_LOCATION_STATUS_BROADCAST_SCHEDULE);

        log.debug("Broadcasting Siege Location status based on expression: 0 0 * ? * *");
    }

    public void checkSiegeStart(int locationId) {
        if (AdvCustomConfig.SIEGE_AUTO_RACE && SiegeAutoRace.isAutoSiege(locationId)) {
            SiegeAutoRace.AutoSiegeRace(locationId);
        } else if (getSource(locationId) == null) {
            startSiege(locationId);
        } else if (locationId == 4011) {
            startPreparations();
        }
    }

    public void startPreparations() {
        log.debug("Starting preparations of all source locations");

        // Set siege start timer..
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {

                // Remove players from Tiamaranta's Eye
                World.getInstance().getWorldMap(600040000).getWorldMapInstance().doOnAllObjects(new Visitor<VisibleObject>() {
                    @Override
                    public void visit(VisibleObject object) {
//                        if (!SiegeConfig.ALL_TIME_AVAILABLE_PORTALS) {
                        if (object instanceof Player) {
                            TeleportService.moveToBindLocation((Player) object, true);
                        } else if (object instanceof Kisk) {
                            KiskService.getInstance().removeKisk((Kisk) object);
                        }
//                        }
                    }
                });

                for (SourceLocation source : getSources().values()) {
                    startSiege(source.getLocationId());
                }
            }
        }, 300000);//5 min

        // 10 sec after start all players moved out and send SM_SHIELD_EFFECT & 2nd SM_SIEGE_LOCATION_STATE
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                for (SourceLocation source : getSources().values()) {
                    source.clearLocation();
                }
                World.getInstance().getWorldMap(600030000).getWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {

                    @Override
                    public void visit(Player player) {
                        for (SourceLocation source : getSources().values()) {
                            player.sendPck(new SM_SHIELD_EFFECT(source.getLocationId()));
                        }
                        for (SourceLocation source : getSources().values()) {
                            player.sendPck(new SM_SIEGE_LOCATION_STATE(source.getLocationId(), 2));
                        }
                    }
                });
            }
        }, 310000);

        for (final SourceLocation source : getSources().values()) {
            source.setPreparation(true);

            if (AdvCustomConfig.AUTO_SOURCE_RACE) {
                SiegeAutoRace.AutoSourceRace();
            } else if (!source.getRace().equals(SiegeRace.BALAUR)) {
                deSpawnNpcs(source.getLocationId());

                final int oldOwnerRaceId = source.getRace().getRaceId();
                final int legionId = source.getLegionId();
                final String legionName = legionId != 0 ? LegionService.getInstance().getLegion(legionId).getLegionName() : "";
                final DescId sourceNameId = DescId.of(source.getTemplate().getNameId());

                source.setRace(SiegeRace.BALAUR);
                source.setLegionId(0);

                World.getInstance().doOnAllPlayers(new Visitor<Player>() {

                    @Override
                    public void visit(Player player) {
                        if ((legionId != 0) && (player.getRace().getRaceId() == oldOwnerRaceId)) {
                            player.sendPck(new SM_SYSTEM_MESSAGE(1301037, legionName, sourceNameId));
                        }

                        player.sendPck(new SM_SYSTEM_MESSAGE(1301039, source.getRace().getDescId(), sourceNameId));

                        player.sendPck(new SM_SIEGE_LOCATION_INFO(source));
                    }
                });
                spawnNpcs(source.getLocationId(), SiegeRace.BALAUR, SiegeModType.PEACE);

                GDB.get(SiegeDAO.class).updateSiegeLocation(source);
            }

        }

        updateTiamarantaRiftsStatus(true, false);
    }

    public void startSiege(final int siegeLocationId) {
        log.debug("Starting siege of siege location: " + siegeLocationId);

        // Siege should not be started two times. Never.
        Siege<?> siege;
        synchronized (this) {
            if (activeSieges.containsKey(siegeLocationId)) {
                log.error("Attempt to start siege twice for siege location: " + siegeLocationId);
                return;
            }
            siege = newSiege(siegeLocationId);
            activeSieges.put(siegeLocationId, siege);
        }

        siege.startSiege();

        // certain sieges are endless
        // should end only manually on siege boss death
        if (siege.isEndless()) {
            return;
        }

        // schedule siege end
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                stopSiege(siegeLocationId);
            }
        }, siege.getSiegeLocation().getSiegeDuration() * 1000);
    }

    public void stopSiege(int siegeLocationId) {

        log.debug("Stopping siege of siege location: " + siegeLocationId);

        // Just a check here...
        // If fortresses was captured in 99% the siege timer will return here
        // without concurrent race
        if (!isSiegeInProgress(siegeLocationId)) {
            log.debug("Siege of siege location " + siegeLocationId + " is not in progress, it was captured earlier?");
            return;
        }

        // We need synchronization here for that 1% of cases :)
        // It may happen that fortresses siege is stopping in the same time by 2 different threads
        // 1 is for killing the boss
        // 2 is for the schedule
        // it might happen that siege will be stopping by other thread, but in such case siege object will be null
        Siege<?> siege;
        synchronized (this) {
            siege = activeSieges.remove(siegeLocationId);
        }
        if (siege == null || siege.isFinished()) {
            return;
        }

        siege.stopSiege();
    }

    /**
     * Updates next state for fortresses
     */
    protected void updateFortressNextState() {

        // get current hour and add 1 hour
        Calendar currentHourPlus1 = Calendar.getInstance();
        currentHourPlus1.set(Calendar.MINUTE, 0);
        currentHourPlus1.set(Calendar.SECOND, 0);
        currentHourPlus1.set(Calendar.MILLISECOND, 0);
        currentHourPlus1.add(Calendar.HOUR, 1);

        // filter fortress siege start runnables
        Map<Runnable, JobDetail> siegeStartRunables = CronService.getInstance().getRunnables();
        siegeStartRunables = Maps.filterKeys(siegeStartRunables, new Predicate<Runnable>() {

            @Override
            public boolean apply(@Nullable Runnable runnable) {
                return (runnable instanceof SiegeStartRunnable);
            }
        });

        // Create map FortressId-To-AllTriggers
        Map<Integer, List<Trigger>> siegeIdToStartTriggers = Maps.newHashMap();
        for (Map.Entry<Runnable, JobDetail> entry : siegeStartRunables.entrySet()) {
            SiegeStartRunnable fssr = (SiegeStartRunnable) entry.getKey();

            List<Trigger> storage = siegeIdToStartTriggers.get(fssr.getLocationId());
            if (storage == null) {
                storage = Lists.newArrayList();
                siegeIdToStartTriggers.put(fssr.getLocationId(), storage);
            }
            storage.addAll(CronService.getInstance().getJobTriggers(entry.getValue()));
        }

        // update each fortress next state
        for (Map.Entry<Integer, List<Trigger>> entry : siegeIdToStartTriggers.entrySet()) {

            List<Date> nextFireDates = Lists.newArrayListWithCapacity(entry.getValue().size());
            for (Trigger trigger : entry.getValue()) {
                nextFireDates.add(trigger.getNextFireTime());
            }
            Collections.sort(nextFireDates);

            // clear non-required times
            Date nextSiegeDate = nextFireDates.get(0);
            Calendar siegeStartHour = Calendar.getInstance();
            siegeStartHour.setTime(nextSiegeDate);
            siegeStartHour.set(Calendar.MINUTE, 0);
            siegeStartHour.set(Calendar.SECOND, 0);
            siegeStartHour.set(Calendar.MILLISECOND, 0);

            // update fortress state that will be valid in 1 h
            SiegeLocation fortress = getSiegeLocation(entry.getKey());
            // check if siege duration is > than 1 Hour
            Calendar siegeCalendar = Calendar.getInstance();
            siegeCalendar.set(Calendar.MINUTE, 0);
            siegeCalendar.set(Calendar.SECOND, 0);
            siegeCalendar.set(Calendar.MILLISECOND, 0);
            siegeCalendar.add(Calendar.HOUR, 0);
            siegeCalendar.add(Calendar.SECOND, getRemainingSiegeTimeInSeconds(fortress.getLocationId()));

            if (fortress instanceof SourceLocation || AdvCustomConfig.SIEGE_AUTO_RACE && SiegeAutoRace.isAutoSiege(fortress.getLocationId())) {
                siegeStartHour.add(Calendar.HOUR, 1);
            }
            if (currentHourPlus1.getTimeInMillis() == siegeStartHour.getTimeInMillis() || siegeCalendar.getTimeInMillis() > currentHourPlus1.getTimeInMillis()) {
                fortress.setNextState(1);
            } else {
                fortress.setNextState(0);
            }
        }
    }

    /**
     * TODO: WTF is it?
     *
     * @return seconds before hour end
     */
    public int getSecondsBeforeHourEnd() {
        Calendar c = Calendar.getInstance();
        int minutesAsSeconds = c.get(Calendar.MINUTE) * 60;
        int seconds = c.get(Calendar.SECOND);
        return 3600 - (minutesAsSeconds + seconds);
    }

    /**
     * TODO: Check if it's valid
     * <p/>
     * If siege duration is endless - will return -1
     *
     * @param siegeLocationId Scheduled siege end time
     *
     * @return remaining seconds in current hour
     */
    public int getRemainingSiegeTimeInSeconds(int siegeLocationId) {

        Siege<?> siege = getSiege(siegeLocationId);
        if (siege == null || siege.isFinished()) {
            return 0;
        }

        if (!siege.isStarted()) {
            return siege.getSiegeLocation().getSiegeDuration();
        }

        // endless siege
        if (siege.getSiegeLocation().getSiegeDuration() == -1) {
            return -1;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, siege.getSiegeLocation().getSiegeDuration());

        int result = (int) ((calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000);
        return result > 0 ? result : 0;
    }

    public Siege<?> getSiege(SiegeLocation loc) {
        return activeSieges.get(loc.getLocationId());
    }

    public Siege<?> getSiege(Integer siegeLocationId) {
        return activeSieges.get(siegeLocationId);
    }

    public boolean isSiegeInProgress(int fortressId) {
        return activeSieges.containsKey(fortressId);
    }

    public Map<Integer, OutpostLocation> getOutposts() {
        return outposts;
    }

    public OutpostLocation getOutpost(int id) {
        return outposts.get(id);
    }

    public Map<Integer, SourceLocation> getSources() {
        return sources;
    }

    public SourceLocation getSource(int id) {
        return sources.get(id);
    }

    public Map<Integer, FortressLocation> getFortresses() {
        return fortresses;
    }

    public FortressLocation getFortress(int fortressId) {
        return fortresses.get(fortressId);
    }

    public Map<Integer, ArtifactLocation> getArtifacts() {
        return artifacts;
    }

    public ArtifactLocation getArtifact(int id) {
        return getArtifacts().get(id);
    }

    public Map<Integer, ArtifactLocation> getStandaloneArtifacts() {
        return Maps.filterValues(artifacts, new Predicate<ArtifactLocation>() {

            @Override
            public boolean apply(@Nullable ArtifactLocation input) {
                return input != null && input.isStandAlone();
            }

        });
    }

    public Map<Integer, ArtifactLocation> getFortressArtifacts() {
        return Maps.filterValues(artifacts, new Predicate<ArtifactLocation>() {

            @Override
            public boolean apply(@Nullable ArtifactLocation input) {
                return input != null && input.getOwningFortress() != null;
            }

        });
    }

    public Map<Integer, SiegeLocation> getSiegeLocations() {
        return locations;
    }

    public SiegeLocation getSiegeLocation(int locationId) {
        return locations.get(locationId);
    }

    public Map<Integer, SiegeLocation> getSiegeLocations(int worldId) {
        Map<Integer, SiegeLocation> mapLocations = new FastMap<>();
        for (SiegeLocation location : getSiegeLocations().values()) {
            if (location.getWorldId() == worldId) {
                mapLocations.put(location.getLocationId(), location);
            }
        }
        return mapLocations;
    }

    protected Siege<?> newSiege(int siegeLocationId) {
        if (fortresses.containsKey(siegeLocationId)) {
            return new FortressSiege(fortresses.get(siegeLocationId));
        }
        if (sources.containsKey(siegeLocationId)) {
            return new SourceSiege(sources.get(siegeLocationId));
        }
        if (outposts.containsKey(siegeLocationId)) {
            return new OutpostSiege(outposts.get(siegeLocationId));
        }
        if (artifacts.containsKey(siegeLocationId)) {
            return new ArtifactSiege(artifacts.get(siegeLocationId));
        }
        throw new SiegeException("Unknown siege handler for siege location: " + siegeLocationId);
    }

    public void cleanLegionId(int legionId) {
        for (SiegeLocation loc : this.getSiegeLocations().values()) {
            if (loc.getLegionId() == legionId) {
                loc.setLegionId(0);
                break;
            }
        }
    }

    public void updateOutpostStatusByFortress(FortressLocation fortress) {
        for (OutpostLocation outpost : getOutposts().values()) {
            if (outpost.getFortressDependency().contains(fortress.getLocationId())) {
                SiegeRace fortressRace = fortress.getRace();
                for (Integer fortressId : outpost.getFortressDependency()) {
                    SiegeRace sr = getFortresses().get(fortressId).getRace();
                    if (fortressRace != sr) {
                        fortressRace = SiegeRace.BALAUR;
                        break;
                    }

                }

                boolean isSpawned = outpost.isSilentraAllowed();
                SiegeRace newOwnerRace;
                if (SiegeRace.BALAUR == fortressRace) {
                    newOwnerRace = SiegeRace.BALAUR;
                } else {
                    newOwnerRace = fortressRace == SiegeRace.ELYOS ? SiegeRace.ASMODIANS : SiegeRace.ELYOS;
                }

                if (outpost.getRace() != newOwnerRace) {
                    stopSiege(outpost.getLocationId());
                    deSpawnNpcs(outpost.getLocationId());

                    outpost.setRace(newOwnerRace);
                    GDB.get(SiegeDAO.class).updateSiegeLocation(outpost);
                    broadcastStatusAndUpdate(outpost, isSpawned);

                    if (SiegeRace.BALAUR != outpost.getRace()) {
                        if (outpost.isSiegeAllowed()) {
                            startSiege(outpost.getLocationId());
                        } else {
                            spawnNpcs(outpost.getLocationId(), outpost.getRace(), SiegeModType.PEACE);
                        }
                    }
                }
            }
        }
    }

    public void updateTiamarantaRiftsStatus(boolean isPreparation, boolean isSync) {
        int sourceState = 0;
        int aSources = 0;
        int eSources = 0;

        if (isPreparation) {
            broadcastStatusAndUpdate(aSources, eSources, isPreparation, isSync);
        } else {
            for (SourceLocation source : getSources().values()) {
                sourceState += (source.isVulnerable() ? 0 : 1);
                if (source.getRace().equals(SiegeRace.ASMODIANS)) {
                    aSources++;
                } else if (source.getRace().equals(SiegeRace.ELYOS)) {
                    eSources++;
                }
            }

            if (sourceState == 4) {
                broadcastStatusAndUpdate(aSources, eSources, isPreparation, isSync);
            }
        }
    }

    public void spawnTiamarantaPortels(boolean cl, boolean cr, boolean tl, boolean tr) {
        if (cl) {
            SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(600030000, 701286, 1524.45F, 1250.425F, 247.048F, (byte) 60);
            template.setStaticId(1594);
            tiamarantaPortals.put(701286, SpawnEngine.spawnObject(template, 1));
        }
        if (cr) {
            SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(600030000, 701287, 1526.465F, 1784.999F, 250.436F, (byte) 60);
            template.setStaticId(2282);
            tiamarantaPortals.put(701287, SpawnEngine.spawnObject(template, 1));
        }
        if (tl) {
            SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(600030000, 701288, 116.665F, 1543.754F, 295.99701F, (byte) 0);
            template.setStaticId(681);
            tiamarantaPortals.put(701288, SpawnEngine.spawnObject(template, 1));
        }
        if (tr) {
            SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(600030000, 701289, 117.26F, 1929.155F, 295.69101F, (byte) 0);
            template.setStaticId(680);
            tiamarantaPortals.put(701289, SpawnEngine.spawnObject(template, 1));
        }
    }

    private void deSpawnTiamarantaPortals() {
        for (VisibleObject portal : tiamarantaPortals.values()) {
            portal.getController().onDelete();
        }
        cl = (cr = tl = tr = false);

        for (VisibleObject boss : tiamarantaEyeBoss.values()) {
            boss.getController().onDelete();
        }
        tiamarantaPortals.clear();
        tiamarantaEyeBoss.clear();
    }

    public void spawnNpcs(int siegeLocationId, SiegeRace race, SiegeModType type) {
        List<SpawnGroup2> siegeSpawns = DataManager.SPAWNS_DATA2.getSiegeSpawnsByLocId(siegeLocationId);
        for (SpawnGroup2 group : siegeSpawns) {
            for (SpawnTemplate template : group.getSpawnTemplates()) {
                SiegeSpawnTemplate siegetemplate = (SiegeSpawnTemplate) template;
                if (siegetemplate.getSiegeRace().equals(race) && siegetemplate.getSiegeModType().equals(type)) {
                    SpawnEngine.spawnObject(siegetemplate, 1);
                }
            }
        }
    }

    public void spawnNpcsWithoutType(int siegeLocationId, SiegeRace race, SiegeModType type, AbyssNpcType npcType) {
        List<SpawnGroup2> siegeSpawns = DataManager.SPAWNS_DATA2.getSiegeSpawnsByLocId(siegeLocationId);
        for (SpawnGroup2 group : siegeSpawns) {

            if (DataManager.NPC_DATA.getNpcTemplate(group.getNpcId()).getAbyssNpcType().equals(npcType)) {
                continue;
            }

            for (SpawnTemplate template : group.getSpawnTemplates()) {
                SiegeSpawnTemplate siegetemplate = (SiegeSpawnTemplate) template;
                if (siegetemplate.getSiegeRace().equals(race) && siegetemplate.getSiegeModType().equals(type)) {
                    SpawnEngine.spawnObject(siegetemplate, 1);
                }
            }
        }
    }

    public void spawnNpcByType(int siegeLocationId, SiegeRace race, SiegeModType siegeType, AbyssNpcType npcType) {
        List<SpawnGroup2> siegeSpawns = DataManager.SPAWNS_DATA2.getSiegeSpawnsByLocId(siegeLocationId);
        for (SpawnGroup2 group : siegeSpawns) {

            if (!DataManager.NPC_DATA.getNpcTemplate(group.getNpcId()).getAbyssNpcType().equals(npcType)) {
                continue;
            }

            for (SpawnTemplate template : group.getSpawnTemplates()) {
                SiegeSpawnTemplate siegetemplate = (SiegeSpawnTemplate) template;
                if (siegetemplate.getSiegeRace().equals(race) && siegetemplate.getSiegeModType().equals(siegeType)) {
                    SpawnEngine.spawnObject(siegetemplate, 1);
                }
            }
        }
    }

    public void deSpawnNpcs(int siegeLocationId) {
        Collection<SiegeNpc> siegeNpcs = World.getInstance().getLocalSiegeNpcs(siegeLocationId);
        for (SiegeNpc npc : siegeNpcs) {
            npc.getController().onDelete();
        }
    }

    public boolean isSiegeNpcInActiveSiege(Npc npc) {
        if ((npc instanceof SiegeNpc)) {
            FortressLocation fort = getFortress(((SiegeNpc) npc).getSiegeId());
            if (fort != null) {
                if (fort.isVulnerable()) {
                    return true;
                }
                if (fort.getNextState() == 1) {
                    return npc.getSpawn().getRespawnTime() >= getSecondsBeforeHourEnd();
                }
            }
        }
        return false;
    }

    public void broadcastUpdate() {
        broadcast(new SM_SIEGE_LOCATION_INFO(), null);
    }

    public void broadcastUpdate(SiegeLocation loc) {
        Influence.getInstance().recalculateInfluence();
        broadcast(new SM_SIEGE_LOCATION_INFO(loc), new SM_INFLUENCE_RATIO());
    }

    public void broadcast(final AionServerPacket pkt1, final AionServerPacket pkt2) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                if (pkt1 != null) {
                    player.sendPck(pkt1);
                }
                if (pkt2 != null) {
                    player.sendPck(pkt2);
                }
            }
        });
    }

    public void broadcastUpdate(SiegeLocation loc, DescId nameId) {
        SM_SIEGE_LOCATION_INFO pkt = new SM_SIEGE_LOCATION_INFO(loc);
        SM_SYSTEM_MESSAGE info = loc.getLegionId() == 0 ? new SM_SYSTEM_MESSAGE(1301039, loc.getRace().getDescId(), nameId)
                : new SM_SYSTEM_MESSAGE(1301038, LegionService.getInstance().getLegion(loc.getLegionId()).getLegionName(), nameId);

        broadcast(pkt, info, loc.getRace());
    }

    private void broadcast(final AionServerPacket pkt, final AionServerPacket info, final SiegeRace race) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                if (player.getRace().getRaceId() == race.getRaceId()) {
                    player.sendPck(info);
                }
                player.sendPck(pkt);
            }
        });
    }

    public void broadcastStatusAndUpdate(OutpostLocation outpost, boolean oldSilentraState) {
        SM_SYSTEM_MESSAGE info = null;
        if (oldSilentraState != outpost.isSilentraAllowed()) {
            if (outpost.isSilentraAllowed()) {
                info = outpost.getLocationId() == 2111 ? SM_SYSTEM_MESSAGE.STR_FIELDABYSS_LIGHTUNDERPASS_SPAWN
                        : SM_SYSTEM_MESSAGE.STR_FIELDABYSS_DARKUNDERPASS_SPAWN;
            } else {
                info = outpost.getLocationId() == 2111 ? SM_SYSTEM_MESSAGE.STR_FIELDABYSS_LIGHTUNDERPASS_DESPAWN
                        : SM_SYSTEM_MESSAGE.STR_FIELDABYSS_DARKUNDERPASS_DESPAWN;
            }
        }
        if (!SiegeConfig.ALL_TIME_AVAILABLE_PORTALS) {
            broadcast(new SM_RIFT_ANNOUNCE(getOutpost(3111).isSilentraAllowed(), getOutpost(2111).isSilentraAllowed()), info);
        }
    }

    public void broadcastStatusAndUpdate(int aSources, int eSources, boolean isPreparation, boolean isSync) {
        if (!SiegeConfig.ALL_TIME_AVAILABLE_PORTALS) {
            deSpawnTiamarantaPortals();
        }
        cl = eSources > 1;
        cr = aSources > 1;

        if (!SiegeConfig.ALL_TIME_AVAILABLE_PORTALS) {
            if (isSync) {
                spawnTiamarantaPortels(cl, cr, tl = cl, tr = cr);
            } else if ((!isPreparation) && ((cl) || (cr))) {
                ThreadPoolManager.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        if ((!tl) || (!tr)) {
                            spawnTiamarantaPortels(false, false, tl = cl, tr = cr);
                            broadcast(new SM_RIFT_ANNOUNCE(cl, cr, tl, tr), null);
                        }
                    }
                }, 5400000);

                spawnTiamarantaPortels(cl, cr, false, false);
            }
            broadcast(new SM_RIFT_ANNOUNCE(cl, cr, tl, tr), null);
        }
    }

    private void broadcast(final SM_RIFT_ANNOUNCE rift, final SM_SYSTEM_MESSAGE info) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                player.sendPck(rift);
                if (info != null && player.getWorldType().equals(WorldType.BALAUREA)) {
                    player.sendPck(info);
                }
            }
        });
    }

    public void validateLoginZone(Player player) {
        if (!SiegeConfig.ALL_TIME_AVAILABLE_PORTALS) {
            BindPointPosition bind = player.getBindPoint();
            int mapId;
            float x;
            float y;
            float z;
            int h;
            if (bind != null) {
                mapId = bind.getMapId();
                x = bind.getX();
                y = bind.getY();
                z = bind.getZ();
                h = bind.getHeading();
            } else {
                PlayerInitialData.LocationData start = DataManager.PLAYER_INITIAL_DATA.getSpawnLocation(player.getRace());

                mapId = start.getMapId();
                x = start.getX();
                y = start.getY();
                z = start.getZ();
                h = start.getHeading();
            }

            if (player.getWorldId() == 600040000) {
                if (player.getRace() == Race.ELYOS ? !cl : !cr && !getSource(4011).isPreparations()) {
                    World.getInstance().setPosition(player, mapId, x, y, z, h);
                }
                return;
            }

            for (FortressLocation fortress : getFortresses().values()) {
                if (fortress.isInActiveSiegeZone(player) && fortress.isEnemy(player)) {
                    World.getInstance().setPosition(player, mapId, x, y, z, h);
                    return;
                }
            }
            for (SourceLocation source : getSources().values()) {
                if (source.isInActiveSiegeZone(player)) {
                    WorldPosition pos = source.getEntryPosition();
                    World.getInstance().setPosition(player, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ(), pos.getH());
                    return;
                }
            }
        }
    }

    public void onPlayerLogin(Player player) {
        if (SiegeConfig.SIEGE_ENABLED) {
            player.sendPck(new SM_INFLUENCE_RATIO());
            player.sendPck(new SM_SIEGE_LOCATION_INFO());
            player.sendPck(new SM_RIFT_ANNOUNCE(getOutpost(3111).isSilentraAllowed(), getOutpost(2111).isSilentraAllowed()));

            if (!SiegeConfig.ALL_TIME_AVAILABLE_PORTALS) {
                player.sendPck(new SM_RIFT_ANNOUNCE(cl, cr, tl, tr));
            } else {
                player.sendPck(new SM_RIFT_ANNOUNCE(true, true, true, true));
            }
        }
    }

    public void onEnterSiegeWorld(Player player) {
        FastMap<Integer, SiegeLocation> worldLocations = new FastMap<>();
        FastMap<Integer, ArtifactLocation> worldArtifacts = new FastMap<>();

        for (SiegeLocation location : getSiegeLocations().values()) {
            if (location.getWorldId() == player.getWorldId()) {
                worldLocations.put(location.getLocationId(), location);
            }
        }
        for (ArtifactLocation artifact : getArtifacts().values()) {
            if (artifact.getWorldId() == player.getWorldId()) {
                worldArtifacts.put(artifact.getLocationId(), artifact);
            }
        }
        player.sendPck(new SM_SHIELD_EFFECT(worldLocations.values()));
        player.sendPck(new SM_ABYSS_ARTIFACT_INFO3(worldArtifacts.values()));
        player.sendPck(new SM_RIFT_ANNOUNCE(player.getRace()));
    }

    public int getFortressId(int locId) {
        switch (locId) {
            case 49:
            case 61:
                return 1011;
            case 36:
            case 54:
                return 1131;
            case 37:
            case 55:
                return 1132;
            case 39:
            case 56:
                return 1141;
            case 44:
            case 62:
                return 1211;
            case 45:
            case 57:
            case 72:
            case 75:
                return 1221;
            case 46:
            case 58:
            case 73:
            case 76:
                return 1231;
            case 47:
            case 59:
            case 74:
            case 77:
                return 1241;
            case 48:
            case 60:
                return 1251;
            case 90:
                return 2011;
            case 91:
                return 2021;
            case 93:
                return 3011;
            case 94:
                return 3021;
        }
        return 0;
    }

    // 25.05.2013 Jenelli. При смерти боса обязательно вызывать этот метод чтобы рассчитать дальнейший респ.
    // Если моб был просто заспаунен(например командой), то никаких мер предпринимать не надо.
    public void despawnTiamarantaBoss() {
        //Добавлять в задачу только если он был в босах!
        if (isSpawnGovernorSunayaka218553 != null) {
            isSpawnGovernorSunayaka218553 = null;
            addRespawnTask();
        } else {
            log.info("[Governor Sunayaka 218553] Npc is already despawned.");
        }
    }

    public void addRespawnTask() {
        int hourFrom = GOVERNOR_SUNAYAKA_RESPAWN_FROM;
        int hourTo = GOVERNOR_SUNAYAKA_RESPAWN_TO;
        if (hourFrom <= 0 || hourTo <= hourFrom) {
            hourFrom = 24;
            hourTo = 30;
        }
        int minBetween = (hourTo - hourFrom) * 60; // с точносью до минуты будет спавнить
        int minRandom = Math.abs(Rnd.nextInt()) % minBetween;
        long nextSpawn = System.currentTimeMillis() + (hourFrom * 60 + minRandom) * 60 * 1000L;
        String cronSpawn = DateUtil.format("ss mm HH dd MM ? yyyy", nextSpawn);
        log.info("[Governor Sunayaka 218553] Next spawn at " + cronSpawn);

        ArrayList<TaskFromDB> tasks = GDB.get(TaskFromDBDAO.class).getTasksByNameAndNpcId("spawn", SiegeBoss.GOVERNOR_SUNAYAKA_218553);
        if (tasks == null || tasks.size() != 1) {
            log.warn("[Governor Sunayaka 218553] Count of task with that npc != 1... After restart server Sunayaka willn't spawn.");
        } else {
            for (TaskFromDB task : tasks) {
                GDB.get(TaskFromDBDAO.class).setStartTime(cronSpawn, task.getId());
                break;
            }
        }

        CronService.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (SiegeService.getInstance().spawnTiamarantaBoss()) {
                    log.info("[Governor Sunayaka 218553] Spawned npc.");
                } else {
                    log.info("[Governor Sunayaka 218553] Cann't spawn npc.");
                }
            }
        }, cronSpawn);
    }

    public boolean spawnTiamarantaBoss() {
        if (isSpawnGovernorSunayaka218553 != null) {
            log.info("[Governor Sunayaka 218553] Npc is already spawned...");
            return false;
        }
        if (tiamarantaEyeBoss.containsKey(SiegeBoss.BERSERKER_SUNAYAKA_219359) && tiamarantaEyeBoss.get(SiegeBoss.BERSERKER_SUNAYAKA_219359).isSpawned()) {
            log.info("[Governor Sunayaka 218553] Npc Berserker Sunayaka is already spawned...");
            addRespawnTask();
            return false;
        }
        isSpawnGovernorSunayaka218553 = SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(
                600040000,
                SiegeBoss.GOVERNOR_SUNAYAKA_218553,
                759.09583F,
                765.68158F,
                1226.5004F,
                (byte) 0
        ), 1);
        return true;
    }
}
