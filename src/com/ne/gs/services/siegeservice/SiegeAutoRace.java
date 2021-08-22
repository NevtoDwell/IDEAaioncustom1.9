/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.gs.configs.main.AdvCustomConfig;
import com.ne.gs.database.dao.SiegeDAO;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.siege.FortressLocation;
import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.siege.SourceLocation;
import com.ne.gs.network.aion.serverpackets.SM_SIEGE_LOCATION_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.LegionService;
import com.ne.gs.services.SiegeService;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

public final class SiegeAutoRace {

    private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
    private static final String[] siegeIds = AdvCustomConfig.SIEGE_AUTO_LOCID.split(";");
    private static final String[] sourceIds = AdvCustomConfig.SIEGE_AUTO_LOCID.split(";");

    public static void AutoSourceRace() {

        log.debug("Starting preparations of all source locations");

        for (final SourceLocation source : SiegeService.getInstance().getSources().values()) {
            if (!source.getRace().equals(SiegeRace.ASMODIANS) || !source.getRace().equals(SiegeRace.ELYOS)) {
                SiegeService.getInstance().deSpawnNpcs(source.getLocationId());
                final int oldOwnerRaceId = source.getRace().getRaceId();
                final int legionId = source.getLegionId();
                final String legionName = legionId != 0 ? LegionService.getInstance().getLegion(legionId).getLegionName() : "";
                final DescId sourceNameId = DescId.of(source.getTemplate().getNameId());

                if (ElyAutoSource(source.getLocationId())) {
                    source.setRace(SiegeRace.ELYOS);
                }
                if (AmoAutoSource(source.getLocationId())) {
                    source.setRace(SiegeRace.ASMODIANS);
                }
                source.setLegionId(0);

                World.getInstance().doOnAllPlayers(new Visitor<Player>() {

                    @Override
                    public void visit(Player player) {
                        if (legionId != 0 && player.getRace().getRaceId() == oldOwnerRaceId) {
                            player.sendPck(new SM_SYSTEM_MESSAGE(1301037, legionName, sourceNameId));
                        }
                        player.sendPck(new SM_SYSTEM_MESSAGE(1301039, source.getRace().getDescId(), sourceNameId));
                        player.sendPck(new SM_SIEGE_LOCATION_INFO(source));
                    }
                });
                if (ElyAutoSource(source.getLocationId())) {
                    SiegeService.getInstance().spawnNpcs(source.getLocationId(), SiegeRace.ELYOS, SiegeModType.PEACE);
                } else if (AmoAutoSource(source.getLocationId())) {
                    SiegeService.getInstance().spawnNpcs(source.getLocationId(), SiegeRace.ASMODIANS, SiegeModType.PEACE);
                }
                GDB.get(SiegeDAO.class).updateSiegeLocation(source);
            }
        }
        SiegeService.getInstance().updateTiamarantaRiftsStatus(false, true);
    }

    public static void AutoSiegeRace(final int locid) {
        final SiegeLocation loc = SiegeService.getInstance().getSiegeLocation(locid);
        if (!loc.getRace().equals(SiegeRace.ASMODIANS) || !loc.getRace().equals(SiegeRace.ELYOS)) {
            ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    SiegeService.getInstance().startSiege(locid);
                }
            }, 300000);

            SiegeService.getInstance().deSpawnNpcs(locid);

            final int oldOwnerRaceId = loc.getRace().getRaceId();
            final int legionId = loc.getLegionId();
            final String legionName = legionId != 0 ? LegionService.getInstance().getLegion(legionId).getLegionName() : "";
            final DescId NameId = DescId.of(loc.getTemplate().getNameId());
            if (ElyAutoSiege(locid)) {
                loc.setRace(SiegeRace.ELYOS);
            }
            if (AmoAutoSiege(locid)) {
                loc.setRace(SiegeRace.ASMODIANS);
            }
            loc.setLegionId(0);
            World.getInstance().doOnAllPlayers(new Visitor<Player>() {

                @Override
                public void visit(Player player) {
                    if (legionId != 0 && player.getRace().getRaceId() == oldOwnerRaceId) {
                        player.sendPck(new SM_SYSTEM_MESSAGE(1301037, legionName, NameId));
                    }
                    player.sendPck(new SM_SYSTEM_MESSAGE(1301039, loc.getRace().getDescId(), NameId));
                    player.sendPck(new SM_SIEGE_LOCATION_INFO(loc));
                }
            });
            if (ElyAutoSiege(locid)) {
                SiegeService.getInstance().spawnNpcs(locid, SiegeRace.ELYOS, SiegeModType.PEACE);
            } else if (AmoAutoSiege(locid)) {
                SiegeService.getInstance().spawnNpcs(locid, SiegeRace.ASMODIANS, SiegeModType.PEACE);
            }
            GDB.get(SiegeDAO.class).updateSiegeLocation(loc);
            SiegeService.getInstance().updateOutpostStatusByFortress((FortressLocation) loc);
        }
        SiegeService.getInstance().broadcastUpdate(loc);
    }

    public static boolean isAutoSiege(int locId) {
        return ElyAutoSiege(locId) || AmoAutoSiege(locId);
    }

    public static boolean ElyAutoSiege(int locId) {
        for (String id : siegeIds[0].split(",")) {
            if (locId == Integer.parseInt(id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean AmoAutoSiege(int locId) {
        for (String id : siegeIds[1].split(",")) {
            if (locId == Integer.parseInt(id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAutoSource(int locId) {
        return ElyAutoSource(locId) || AmoAutoSource(locId);
    }

    public static boolean ElyAutoSource(int locId) {
        for (String id : sourceIds[0].split(",")) {
            if (locId == Integer.parseInt(id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean AmoAutoSource(int locId) {
        for (String id : sourceIds[1].split(",")) {
            if (locId == Integer.parseInt(id)) {
                return true;
            }
        }
        return false;
    }
}
