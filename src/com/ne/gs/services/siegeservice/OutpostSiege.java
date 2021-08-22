/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import java.util.Map;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.siege.OutpostLocation;
import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.SiegeService;
import com.ne.gs.services.player.PlayerService;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

public class OutpostSiege extends Siege<OutpostLocation> {

    /**
     * TODO: This should be removed
     */

    public OutpostSiege(OutpostLocation siegeLocation) {
        super(siegeLocation);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onSiegeStart() {
        SiegeService.getInstance().deSpawnNpcs(getSiegeLocationId());

        getSiegeLocation().setVulnerable(true);

        SiegeService.getInstance().spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.SIEGE);
        initSiegeBoss();

        // TODO: Refactor me
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                AionServerPacket packet = new SM_SYSTEM_MESSAGE(getSiegeLocationId() == 2111 ? 1400317 : 1400318);
                player.sendPck(packet);
            }
        });

        broadcastUpdate(getSiegeLocation());
    }

    @Override
    protected void onSiegeFinish() {
        getSiegeLocation().setVulnerable(false);
        unregisterSiegeBossListeners();

        // TODO: Refactor messages
        if (isBossKilled()) {
            onCapture();
        } else {
            World.getInstance().doOnAllPlayers(new Visitor<Player>() {

                @Override
                public void visit(Player player) {
                    AionServerPacket packet = new SM_SYSTEM_MESSAGE(getSiegeLocationId() == 2111 ? 1400319 : 1400320);
                    player.sendPck(packet);
                }

            });
        }

        broadcastUpdate(getSiegeLocation());
    }

    private void onCapture() {
        SiegeRaceCounter winnerCounter = getSiegeCounter().getWinnerRaceCounter();
        Map<Integer, Long> topPlayerDamages = winnerCounter.getPlayerDamageCounter();
        if (!topPlayerDamages.isEmpty()) {

            // prepare top player
            Integer topPlayer = topPlayerDamages.keySet().iterator().next();
            String topPlayerName = PlayerService.getPlayerName(topPlayer);
            // Prepare message for sending to all players
            int messageId = getSiegeLocationId() == 2111 ? 1400324 : 1400323;
            Race race = winnerCounter.getSiegeRace() == SiegeRace.ELYOS ? Race.ELYOS : Race.ASMODIANS;
            final AionServerPacket asp = new SM_SYSTEM_MESSAGE(messageId, race, topPlayerName);

            // send packet for all players
            World.getInstance().doOnAllPlayers(new Visitor<Player>() {

                @Override
                public void visit(Player player) {
                    player.sendPck(asp);
                }
            });
        }
    }

    @Override
    public boolean isEndless() {
        return false;
    }

    @Override
    public void addAbyssPoints(Player player, int abysPoints) {
    }
}
