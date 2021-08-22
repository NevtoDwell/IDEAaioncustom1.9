/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.ne.gs.utils.chathandlers.ChatProcessor;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.ne.gs.database.GDB;
import com.ne.gs.services.custom.CustomQuestsService;
import mw.engines.geo.GeoEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.Sys;
import com.ne.commons.database.DatabaseFactory;
import com.ne.commons.network.NioServer;
import com.ne.commons.network.ServerCfg;
import com.ne.commons.services.CronService;
import com.ne.commons.utils.L10N;
import com.ne.gs.ai2.AI2Engine;
import com.ne.gs.cache.HTMLCache;
import com.ne.gs.configs.Config;
import com.ne.gs.configs.main.AIConfig;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.EventsConfig;
import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.configs.main.SiegeConfig;
import com.ne.gs.configs.main.ThreadConfig;
import com.ne.gs.configs.main.WeddingsConfig;
import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.eventNewEngine.events.EventManager;
import com.ne.gs.instance.InstanceEngine;
import com.ne.gs.model.Race;
import com.ne.gs.model.siege.Influence;
import com.ne.gs.modules.common.CustomLocManager;
import com.ne.gs.modules.customrates.CustomRateManager;
import com.ne.gs.modules.customrifts.CustomRiftManager;
import com.ne.gs.modules.customspawner.CustomSpawner;
import com.ne.gs.modules.housing.Housing;
import com.ne.gs.modules.mapskillrestrictor.MapSkillRestrictor;
import com.ne.gs.modules.pvpapaccumulator.PvPApAccumulator;
import com.ne.gs.network.BannedMacManager;
import com.ne.gs.network.aion.GameConnectionFactoryImpl;
import com.ne.gs.network.chatserver.ChatServer;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.services.*;
import com.ne.gs.services.abyss.AbyssRankUpdateService;
import com.ne.gs.services.custom.ItemTimeTuningService;
import com.ne.gs.services.custom.KeyDropTuningService;
import com.ne.gs.services.drop.DropRegistrationService;
import com.ne.gs.services.drop.GlobalDropRegistrationService;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.services.player.PlayerEventService;
import com.ne.gs.services.player.PlayerLimitService;
import com.ne.gs.services.reward.RewardService;
import com.ne.gs.spawnengine.InstanceRiftSpawnManager;
import com.ne.gs.spawnengine.RiftSpawnManager;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.spawnengine.TemporarySpawnEngine;
import com.ne.gs.taskmanager.TaskManagerFromDB;
import com.ne.gs.taskmanager.tasks.PacketBroadcaster;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.ThreadUncaughtExceptionHandler;
import com.ne.gs.utils.cron.ThreadPoolManagerRunnableRunner;
import com.ne.gs.utils.gametime.DateTimeUtil;
import com.ne.gs.utils.gametime.GameTimeManager;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.zone.ZoneService;


/**
 * <tt>GameServer </tt> is the main class of the application and represents the whole game server.<br>
 * This class is also an entry point with main() method.
 *
 * @author -Nemesiss-
 * @author SoulKeeper
 * @author cura
 */
public class GameServer {

    private static final Logger log = LoggerFactory.getLogger(GameServer.class);

    // TODO remove all this shit
    private static int ELYOS_COUNT = 0;
    private static int ASMOS_COUNT = 0;
    private static double ELYOS_RATIO = 0.0;
    private static double ASMOS_RATIO = 0.0;
    private static final ReentrantLock lock = new ReentrantLock();

    private static List<StartupHook> startUpHooks = new ArrayList<>();

    public static void initalizeLoggger() {
        new File("./log/backup/").mkdirs();
        File[] files = new File("log").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".log");
            }
        });

        if (files != null && files.length > 0) {
            byte[] buf = new byte[1024];
            try {
                String outFilename = "./log/backup/" + new SimpleDateFormat("yyyy-MM-dd HHmmss").format(new Date()) + ".zip";
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
                out.setMethod(ZipOutputStream.DEFLATED);
                out.setLevel(Deflater.BEST_COMPRESSION);

                for (File logFile : files) {
                    FileInputStream in = new FileInputStream(logFile);
                    out.putNextEntry(new ZipEntry(logFile.getName()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                    logFile.delete();
                }
                out.close();
            } catch (IOException e) {
            }
        }
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            configurator.doConfigure("config/slf4j-logback.xml");
        } catch (JoranException je) {
            throw new RuntimeException("Failed to configure loggers, shutting down...", je);
        }
    }

    /**
     * Launching method for GameServer
     *
     * @param args arguments, not used
     */
    public static void main(String[] args) {
        // disable Quartz update check
        System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");

        long start = System.currentTimeMillis();

        initalizeLoggger();
        initUtilityServicesAndConfig();

        GDB.getInstance();

        DataManager.getInstance();

        Sys.printSection("IDFactory");
        IDFactory.getInstance();

        Sys.printSection("Zone");
        ZoneService.getInstance().load(null);

        Sys.printSection("Geodata");

        GeoEngine.Initialize();

        System.gc();

        Sys.printSection("World");
        World.getInstance();

        Sys.printSection("Drops");
        DropRegistrationService.getInstance();
        GlobalDropRegistrationService.getInstance();


        // Set all players is offline
        GDB.get(PlayerDAO.class).setPlayersOffline(false);

        DatabaseCleaningService.getInstance();

        BannedMacManager.getInstance();

        QuestEngine.getInstance().load(null);
        InstanceEngine.getInstance().load(null);
        AI2Engine.getInstance().load(null);

        // This is loading only siege location data
        // No Siege schedule or spawns
        Sys.printSection("Siege Location Data");
        SiegeService.getInstance().initSiegeLocations();

        Sys.printSection("Spawns");
        SpawnEngine.spawnAll();
        RiftSpawnManager.spawnAll();
        InstanceRiftSpawnManager.spawnAll();
        TemporarySpawnEngine.spawnAll();

        if (SiegeConfig.SIEGE_ENABLED)
            ShieldService.getInstance().spawnAll();

        Sys.printSection("Limits");
        LimitedItemTradeService.getInstance().start();
        if (CustomConfig.LIMITS_ENABLED) {
            PlayerLimitService.getInstance().scheduleUpdate();
        }
        GameTimeManager.startClock();
        Sys.printSection("Siege Schedule initialization");
        SiegeService.getInstance().initSieges();

        Sys.printSection("TaskManagers");
        PacketBroadcaster.getInstance();

        GameTimeService.getInstance();
        AnnouncementService.getInstance();
        DebugService.getInstance();
        WeatherService.getInstance();
        BrokerService.getInstance();
        Influence.getInstance();
        ExchangeService.getInstance();
        PeriodicSaveService.getInstance();
        PetitionService.getInstance();

        if (AIConfig.SHOUTS_ENABLE) {
            NpcShoutsService.getInstance();
        }
        InstanceService.load();

        FlyRingService.getInstance();
        CuringZoneService.getInstance();
        RoadService.getInstance();
        HTMLCache.getInstance();
        AbyssRankUpdateService.getInstance().scheduleUpdate();
        TaskManagerFromDB.getInstance();
        if (SiegeConfig.SIEGE_SHIELD_ENABLED) {
            ShieldService.getInstance();
        }
        if (CustomConfig.ENABLE_REWARD_SERVICE) {
            RewardService.getInstance();
        }
        if (EventsConfig.EVENT_ENABLED) {
            PlayerEventService.getInstance();
        }
        if (EventsConfig.ENABLE_EVENT_SERVICE) {
            EventService.getInstance().start();
        }
        if (WeddingsConfig.WEDDINGS_ENABLE) {
            WeddingService.getInstance();
        }
        
        if (CustomConfig.EVENT_MODE) {
            EventManager.getInstance().Init();
        }

        QuestRepeatUpdate.getInstance().update();
        AdminService.getInstance();
        ItemTimeTuningService.getInstance();
        KeyDropTuningService.getInstance();
        ChatProcessor.getInstance().Load();

        Housing.load();
        CustomRiftManager.REF.tell(new CustomRiftManager.Init());
        CustomLocManager.getInstance().tell(new CustomLocManager.Init());
        CustomRateManager.getInstance().tell(new CustomRateManager.Init());
        CustomSpawner.REF.tell(new CustomSpawner.Init());

        

        PvPApAccumulator.init();
        MapSkillRestrictor.init();

        Sys.printSection("System");
        CoreVersion.printBuildInfo();

        System.gc();
        Sys.printAllInfos();

        Sys.printSection("GameServerLog");
        log.info("Game Server started in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");

        CustomQuestsService.getInstance();

        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                startServers();
            }
        });

        Runtime.getRuntime().addShutdownHook(ShutdownHook.getInstance());

        if (GSConfig.ENABLE_RATIO_LIMITATION) {
            addStartupHook(new StartupHook() {

                @Override
                public void onStartup() {
                    lock.lock();
                    try {
                        ASMOS_COUNT = GDB.get(PlayerDAO.class).getCharacterCountForRace(Race.ASMODIANS);
                        ELYOS_COUNT = GDB.get(PlayerDAO.class).getCharacterCountForRace(Race.ELYOS);
                        computeRatios();
                    } catch (Exception e) {
                    } finally {
                        lock.unlock();
                    }
                    displayRatios(false);
                }
            });
        }

        addStartupHook(new StartupHook() {
            @Override
            public void onStartup() {
                L10N.loadFromXml("./config/main/l10n.xml");
            }
        });

        onStartup();
    }

    /**
     * Starts servers for connection with aion client and login\chat server.
     */
    private static void startServers() {
        Sys.printSection("Starting Network");
        NioServer nioServer = new NioServer(NetworkConfig.NIO_READ_WRITE_THREADS, new ServerCfg(NetworkConfig.GAME_BIND_ADDRESS, NetworkConfig.GAME_PORT,
                "Game Connections", new GameConnectionFactoryImpl()));

        LoginServer ls = LoginServer.getInstance();
        ChatServer cs = ChatServer.getInstance();

        ls.setNioServer(nioServer);
        cs.setNioServer(nioServer);

        // Nio must go first
        nioServer.connect();
        ls.connect();

        if (GSConfig.ENABLE_CHAT_SERVER) {
            cs.connect();
        }
    }

    /**
     * Initialize all helper services, that are not directly related to aion gs, which includes:
     * <ul>
     * <li>Logging</li>
     * <li>Database factory</li>
     * <li>Thread pool</li>
     * </ul>
     * This method also initializes {@link Config}
     */
    public static void initUtilityServicesAndConfig() {
        // Set default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());

        // Initialize cron service
        CronService.initSingleton(ThreadPoolManagerRunnableRunner.class);

        // init config
        Config.load();
        // DateTime zone override from configs
        DateTimeUtil.init();
        // Second should be database factory
        Sys.printSection("DataBase");
        DatabaseFactory.init();

        // Initialize thread pools
        Sys.printSection("Threads");
        ThreadConfig.load();
        ThreadPoolManager.getInstance();
    }

    public static synchronized void addStartupHook(StartupHook hook) {
        if (startUpHooks != null) {
            startUpHooks.add(hook);
        } else {
            hook.onStartup();
        }
    }

    private synchronized static void onStartup() {
        List<StartupHook> startupHooks = startUpHooks;

        startUpHooks = null;

        for (StartupHook hook : startupHooks) {
            hook.onStartup();
        }
    }

    /**
     * @param race
     * @param i
     */
    public static void updateRatio(Race race, int i) {
        lock.lock();
        try {
            switch (race) {
                case ASMODIANS:
                    ASMOS_COUNT += i;
                    break;
                case ELYOS:
                    ELYOS_COUNT += i;
                    break;
                default:
                    break;
            }

            computeRatios();
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }

        displayRatios(true);
    }

    private static void computeRatios() {
        if (ASMOS_COUNT <= GSConfig.RATIO_MIN_CHARACTERS_COUNT && ELYOS_COUNT <= GSConfig.RATIO_MIN_CHARACTERS_COUNT) {
            ASMOS_RATIO = GameServer.ELYOS_RATIO = 50.0;
        } else {
            ASMOS_RATIO = ASMOS_COUNT * 100.0 / (ASMOS_COUNT + ELYOS_COUNT);
            ELYOS_RATIO = ELYOS_COUNT * 100.0 / (ASMOS_COUNT + ELYOS_COUNT);
        }
    }

    private static void displayRatios(boolean updated) {
        log.info("FACTIONS RATIO " + (updated ? "UPDATED " : "") + ": E " + String.format("%.1f", GameServer.ELYOS_RATIO) + " % / A "
                + String.format("%.1f", GameServer.ASMOS_RATIO) + " %");
    }

    public static double getRatiosFor(Race race) {
        switch (race) {
            case ASMODIANS:
                return ASMOS_RATIO;
            case ELYOS:
                return ELYOS_RATIO;
            default:
                return 0.0;
        }
    }

    public static int getCountFor(Race race) {
        switch (race) {
            case ASMODIANS:
                return ASMOS_COUNT;
            case ELYOS:
                return ELYOS_COUNT;
            default:
                return 0;
        }
    }

    public static abstract interface StartupHook {

        public abstract void onStartup();
    }
}
