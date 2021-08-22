/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.Sys;
import com.ne.commons.configs.CommonsConfig;
import com.ne.commons.configs.DatabaseConfig;
import com.ne.commons.configuration.ConfigurableProcessor;
import com.ne.commons.utils.PropertiesUtils;
import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.configs.administration.DeveloperConfig;
import com.ne.gs.configs.main.*;
import com.ne.gs.configs.modules.AbyssRankConfig;
import com.ne.gs.configs.modules.AnniversaryConfig;
import com.ne.gs.configs.modules.MapSkillRestrictorConfig;
import com.ne.gs.configs.modules.PvPApAccumulatorConfig;
import com.ne.gs.configs.network.IPConfig;
import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.network.aion.Packets;

/**
 * @author Nemesiss, SoulKeeper
 */
public final class Config {

    protected static final Logger log = LoggerFactory.getLogger(Config.class);

    /**
     * Initialize all configs in com.ne.gs.configs package
     */
    public static void load() {
        try {
            Properties myProps = null;
            try {
                log.info("Loading: mygs.properties");
                myProps = PropertiesUtils.load("./config/mygs.properties");
            } catch (Exception e) {
                log.info("No override properties found");
            }

            // Administration
            Sys.printSection("Administration");
            String administration = "./config/administration";

            Properties[] adminProps = PropertiesUtils.loadAllFromDirectory(administration);
            PropertiesUtils.overrideProperties(adminProps, myProps);

            ConfigurableProcessor.process(AdminConfig.class, adminProps);
            log.info("Loading: " + administration + "/admin.properties");

            ConfigurableProcessor.process(DeveloperConfig.class, adminProps);
            log.info("Loading: " + administration + "/developer.properties");

            // Main
            Sys.printSection("Main");
            String main = "./config/main";

            Properties[] mainProps = PropertiesUtils.loadAllFromDirectory(main);
            PropertiesUtils.overrideProperties(mainProps, myProps);

            ConfigurableProcessor.process(AIConfig.class, mainProps);
            log.info("Loading: " + main + "/ai.properties");

            ConfigurableProcessor.process(CommonsConfig.class, mainProps);
            log.info("Loading: " + main + "/commons.properties");

            ConfigurableProcessor.process(CleaningConfig.class, mainProps);
            log.info("Loading: " + main + "/cleaning.properties");

            ConfigurableProcessor.process(CraftConfig.class, mainProps);
            log.info("Loading: " + main + "/craft.properties");

            ConfigurableProcessor.process(CustomConfig.class, mainProps);
            log.info("Loading: " + main + "/custom.properties");

            ConfigurableProcessor.process(DredgionConfig.class, mainProps);
            log.info("Loading: " + main + "/dredgion.properties");

            ConfigurableProcessor.process(DropConfig.class, mainProps);
            log.info("Loading: " + main + "/drop.properties");

            ConfigurableProcessor.process(EnchantsConfig.class, mainProps);
            log.info("Loading: " + main + "/enchants.properties");

            ConfigurableProcessor.process(EventsConfig.class, mainProps);
            log.info("Loading: " + main + "/events.properties");

            ConfigurableProcessor.process(FallDamageConfig.class, mainProps);
            log.info("Loading: " + main + "/falldamage.properties");

            ConfigurableProcessor.process(GSConfig.class, mainProps);
            log.info("Loading: " + main + "/gameserver.properties");

            ConfigurableProcessor.process(GeoDataConfig.class, mainProps);
            log.info("Loading: " + main + "/geodata.properties");

            ConfigurableProcessor.process(GroupConfig.class, mainProps);
            log.info("Loading: " + main + "/group.properties");

            ConfigurableProcessor.process(HousingConfig.class, mainProps);
            log.info("Loading: " + main + "/housing.properties");

            ConfigurableProcessor.process(HTMLConfig.class, mainProps);
            log.info("Loading: " + main + "/html.properties");

            ConfigurableProcessor.process(InGameShopConfig.class, mainProps);
            log.info("Loading: " + main + "/ingameshop.properties");

            ConfigurableProcessor.process(LegionConfig.class, mainProps);
            log.info("Loading: " + main + "/legion.properties");

            ConfigurableProcessor.process(LoggingConfig.class, mainProps);
            log.info("Loading: " + main + "/logging.properties");

            ConfigurableProcessor.process(MembershipConfig.class, mainProps);
            log.info("Loading: " + main + "/membership.properties");

            ConfigurableProcessor.process(NameConfig.class, mainProps);
            log.info("Loading: " + main + "/name.properties");

            ConfigurableProcessor.process(PeriodicSaveConfig.class, mainProps);
            log.info("Loading: " + main + "/periodicsave.properties");

            ConfigurableProcessor.process(PlayerTransferConfig.class, mainProps);
            log.info("Loading: " + main + "/playertransfer.properties");

            ConfigurableProcessor.process(PricesConfig.class, mainProps);
            log.info("Loading: " + main + "/prices.properties");

            ConfigurableProcessor.process(PunishmentConfig.class, mainProps);
            log.info("Loading: " + main + "/punishment.properties");

            ConfigurableProcessor.process(PvPConfig.class, mainProps);
            log.info("Loading: " + main + "/pvp.properties");
            ConfigurableProcessor.process(RankingConfig.class, mainProps);
            log.info("Loading: " + main + "/ranking.properties");

            ConfigurableProcessor.process(RateConfig.class, mainProps);
            log.info("Loading: " + main + "/rates.properties");

            ConfigurableProcessor.process(ShivaConfig.class, mainProps);
            log.info("Loading: " + main + "/shiva.properties");
            ConfigurableProcessor.process(SecurityConfig.class, mainProps);
            log.info("Loading: " + main + "/security.properties");

            ConfigurableProcessor.process(ShutdownConfig.class, mainProps);
            log.info("Loading: " + main + "/shutdown.properties");

            ConfigurableProcessor.process(SiegeConfig.class, mainProps);
            log.info("Loading: " + main + "/siege.properties");

            ConfigurableProcessor.process(ThreadConfig.class, mainProps);
            log.info("Loading: " + main + "/thread.properties");

            ConfigurableProcessor.process(WeddingsConfig.class, mainProps);
            log.info("Loading: " + main + "/weddings.properties");

            ConfigurableProcessor.process(WorldConfig.class, mainProps);
            log.info("Loading: " + main + "/world.properties");
            ConfigurableProcessor.process(AdvCustomConfig.class, mainProps);
            log.info("Loading: " + main + "/advcustom.properties");

            // Network
            Sys.printSection("Network");
            String network = "./config/network";

            Properties[] networkProps = PropertiesUtils.loadAllFromDirectory(network);
            PropertiesUtils.overrideProperties(networkProps, myProps);

            log.info("Loading: " + network + "/database.properties");
            ConfigurableProcessor.process(DatabaseConfig.class, networkProps);
            log.info("Loading: " + network + "/network.properties");
            ConfigurableProcessor.process(NetworkConfig.class, networkProps);

            log.info("Loading: " + network + "/packets.cfg");
            Packets.reload();

            // Modules
            Sys.printSection("Modules");
            String modules = "./config/modules";
            Properties[] modulesProps = PropertiesUtils.loadAllFromDirectory(modules);
            PropertiesUtils.overrideProperties(modulesProps, myProps);

            log.info("Loading: " + modules + "/anniversary.properties");
            ConfigurableProcessor.process(AnniversaryConfig.class, modulesProps);

            log.info("Loading: " + modules + "/abyssrank.properties");
            ConfigurableProcessor.process(AbyssRankConfig.class, modulesProps);

            log.info("Loading: " + modules + "/pvpapaccumulator.properties");
            ConfigurableProcessor.process(PvPApAccumulatorConfig.class, modulesProps);

            log.info("Loading: " + modules + "/mapskillrestrictor.properties");
            ConfigurableProcessor.process(MapSkillRestrictorConfig.class, modulesProps);

        } catch (Exception e) {
            log.error("Can't load gameserver configuration: ", e);
            throw new Error("Can't load gameserver configuration: ", e);
        }

        IPConfig.load();
        
    }

    public static void reload() {
        try {
            Properties myProps = null;
            try {
                log.info("Loading: mygs.properties");
                myProps = PropertiesUtils.load("./config/mygs.properties");
            } catch (Exception e) {
                log.info("No override properties found");
            }

            String administration = "./config/administration";

            Properties[] adminProps = PropertiesUtils.loadAllFromDirectory(administration);
            PropertiesUtils.overrideProperties(adminProps, myProps);

            ConfigurableProcessor.process(AdminConfig.class, adminProps);
            log.info("Reload: " + administration + "/admin.properties");

            ConfigurableProcessor.process(DeveloperConfig.class, adminProps);
            log.info("Reload: " + administration + "/developer.properties");

            String main = "./config/main";

            Properties[] mainProps = PropertiesUtils.loadAllFromDirectory(main);
            PropertiesUtils.overrideProperties(mainProps, myProps);

            ConfigurableProcessor.process(AIConfig.class, mainProps);
            log.info("Reload: " + main + "/ai.properties");

            ConfigurableProcessor.process(CommonsConfig.class, mainProps);
            log.info("Reload: " + main + "/commons.properties");

            ConfigurableProcessor.process(CraftConfig.class, mainProps);
            log.info("Reload: " + main + "/craft.properties");

            ConfigurableProcessor.process(CustomConfig.class, mainProps);
            log.info("Reload: " + main + "/custom.properties");

            ConfigurableProcessor.process(DredgionConfig.class, mainProps);
            log.info("Reload: " + main + "/dredgion.properties");

            ConfigurableProcessor.process(DropConfig.class, mainProps);
            log.info("Reload: " + main + "/drop.properties");

            ConfigurableProcessor.process(EnchantsConfig.class, mainProps);
            log.info("Reload: " + main + "/enchants.properties");

            ConfigurableProcessor.process(EventsConfig.class, mainProps);
            log.info("Reload: " + main + "/events.properties");

            ConfigurableProcessor.process(FallDamageConfig.class, mainProps);
            log.info("Reload: " + main + "/falldamage.properties");

            ConfigurableProcessor.process(GSConfig.class, mainProps);
            log.info("Reload: " + main + "/gameserver.properties");

            ConfigurableProcessor.process(GeoDataConfig.class, mainProps);
            log.info("Reload: " + main + "/geodata.properties");

            ConfigurableProcessor.process(GroupConfig.class, mainProps);
            log.info("Reload: " + main + "/group.properties");

            ConfigurableProcessor.process(HousingConfig.class, mainProps);
            log.info("Reload: " + main + "/housing.properties");

            ConfigurableProcessor.process(HTMLConfig.class, mainProps);
            log.info("Reload: " + main + "/html.properties");

            ConfigurableProcessor.process(InGameShopConfig.class, mainProps);
            log.info("Reload: " + main + "/ingameshop.properties");

            ConfigurableProcessor.process(LegionConfig.class, mainProps);
            log.info("Reload: " + main + "/legion.properties");

            ConfigurableProcessor.process(LoggingConfig.class, mainProps);
            log.info("Reload: " + main + "/logging.properties");

            ConfigurableProcessor.process(MembershipConfig.class, mainProps);
            log.info("Reload: " + main + "/membership.properties");

            ConfigurableProcessor.process(NameConfig.class, mainProps);
            log.info("Reload: " + main + "/name.properties");

            ConfigurableProcessor.process(PeriodicSaveConfig.class, mainProps);
            log.info("Reload: " + main + "/periodicsave.properties");

            ConfigurableProcessor.process(PlayerTransferConfig.class, mainProps);
            log.info("Reload: " + main + "/playertransfer.properties");

            ConfigurableProcessor.process(PricesConfig.class, mainProps);
            log.info("Reload: " + main + "/prices.properties");

            ConfigurableProcessor.process(PunishmentConfig.class, mainProps);
            log.info("Reload: " + main + "/punishment.properties");

            ConfigurableProcessor.process(RankingConfig.class, mainProps);
            log.info("Reload: " + main + "/ranking.properties");

            ConfigurableProcessor.process(RateConfig.class, mainProps);
            log.info("Reload: " + main + "/rates.properties");

            ConfigurableProcessor.process(SecurityConfig.class, mainProps);
            log.info("Reload: " + main + "/security.properties");

            ConfigurableProcessor.process(ShutdownConfig.class, mainProps);
            log.info("Reload: " + main + "/shutdown.properties");

            ConfigurableProcessor.process(SiegeConfig.class, mainProps);
            log.info("Reload: " + main + "/siege.properties");

            ConfigurableProcessor.process(ThreadConfig.class, mainProps);
            log.info("Reload: " + main + "/thread.properties");

            ConfigurableProcessor.process(WeddingsConfig.class, mainProps);
            log.info("Reload: " + main + "/weddings.properties");

            ConfigurableProcessor.process(WorldConfig.class, mainProps);
            log.info("Reload: " + main + "/world.properties");

            ConfigurableProcessor.process(AdvCustomConfig.class, mainProps);
            log.info("Reload: " + main + "/advcustom.properties");

            log.info("Reload: ./config/network/packets.cfg");
            Packets.reload();

            // Modules
            Sys.printSection("Modules");
            String modules = "./config/modules";
            Properties[] modulesProps = PropertiesUtils.loadAllFromDirectory(modules);
            PropertiesUtils.overrideProperties(modulesProps, myProps);

            log.info("Reload: " + modules + "/anniversary.properties");
            ConfigurableProcessor.process(AnniversaryConfig.class, modulesProps);

            log.info("Reload: " + modules + "/abyssrank.properties");
            ConfigurableProcessor.process(AbyssRankConfig.class, modulesProps);

            log.info("Loading: " + modules + "/pvpapaccumulator.properties");
            ConfigurableProcessor.process(PvPApAccumulatorConfig.class, modulesProps);

            log.info("Loading: " + modules + "/mapskillrestrictor.properties");
            ConfigurableProcessor.process(MapSkillRestrictorConfig.class, modulesProps);

        } catch (Exception e) {
            log.error("Can't reload configuration: ", e);
            throw new Error("Can't reload configuration: ", e);
        }

        
    }
}
