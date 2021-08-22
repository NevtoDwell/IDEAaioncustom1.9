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

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.siege.ArtifactLocation;
import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.LegionService;
import com.ne.gs.services.player.PlayerService;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author SoulKeeper
 */
public class ArtifactSiege extends Siege<ArtifactLocation> {

    private static final Logger log = LoggerFactory.getLogger(ArtifactSiege.class.getName());

    public ArtifactSiege(ArtifactLocation siegeLocation) {
        super(siegeLocation);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onSiegeStart() {
        initSiegeBoss();
    }

    @Override
    protected void onSiegeFinish() {

        // cleanup
        unregisterSiegeBossListeners();

        deSpawnNpcs(getSiegeLocationId());
        if (isBossKilled()) {
            onCapture();
        } else {
            log.error("Artifact siege (artifactId:" + getSiegeLocationId() + ") ended without killing a boss.");
        }

        // despawn npcs
        spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.PEACE);

        broadcastUpdate(getSiegeLocation());
        startSiege(getSiegeLocationId());
    }

    protected void onCapture() {
        SiegeRaceCounter wRaceCounter = getSiegeCounter().getWinnerRaceCounter();
        getSiegeLocation().setRace(wRaceCounter.getSiegeRace());

        // update legion
        Integer wLegionId = wRaceCounter.getWinnerLegionId();
        getSiegeLocation().setLegionId(wLegionId != null ? wLegionId : 0);

        // misc stuff to send player system message
        if (getSiegeLocation().getRace() == SiegeRace.BALAUR) {
            // TODO: Fix message for Balaur Description id
            final AionServerPacket lRacePacket = new SM_SYSTEM_MESSAGE(1320004,getSiegeLocation().getNameAsDescriptionId(), getSiegeLocation().getRace().getDescId());
            
            World.getInstance().doOnAllPlayers(new Visitor<Player>() {
                @Override
                public void visit(Player object) {
                    object.sendPck(lRacePacket);
                }
            });

        } else {
            // Prepare packet data
            String wPlayerName = "";
            final Race wRace = wRaceCounter.getSiegeRace() == SiegeRace.ELYOS ? Race.ELYOS : Race.ASMODIANS;
            Legion wLegion = wLegionId != null ? LegionService.getInstance().getLegion(wLegionId) : null;
            if (!wRaceCounter.getPlayerDamageCounter().isEmpty()) {
                Integer wPlayerId = wRaceCounter.getPlayerDamageCounter().keySet().iterator().next();
                wPlayerName = PlayerService.getPlayerName(wPlayerId);
            }
            String winnerName = wLegion != null ? wLegion.getLegionName() : wPlayerName;

            // prepare packets, we can use single packet instance
            final AionServerPacket wRacePacket = new SM_SYSTEM_MESSAGE(1320002, wRace.getRaceDescriptionId(), winnerName, getSiegeLocation()
                .getNameAsDescriptionId());
            final AionServerPacket lRacePacket = new SM_SYSTEM_MESSAGE(1320004, getSiegeLocation().getNameAsDescriptionId(), wRace.getRaceDescriptionId());

            // send update to players
            World.getInstance().doOnAllPlayers(new Visitor<Player>() {
                @Override
                public void visit(Player player) {
                    if(wLegion != null){
                    AionServerPacket packet = player.getRace().equals(wRace) ? wRacePacket : lRacePacket;
                    player.sendPck(packet); 
                    }
                    else if(wLegion == null){
                    AionServerPacket packet = lRacePacket;
                    player.sendPck(packet);
                    }
                }
            });
        }
    }

    @Override
    public boolean isEndless() {
        return true;
    }

    @Override
    public void addAbyssPoints(Player player, int abysPoints) {
    }
}
