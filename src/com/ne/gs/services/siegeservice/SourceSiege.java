/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.ne.gs.database.GDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.EventNotifier;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.configs.main.SiegeConfig;
import com.ne.gs.database.dao.SiegeDAO;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.siege.SourceLocation;
import com.ne.gs.model.templates.npc.AbyssNpcType;
import com.ne.gs.model.templates.siegelocation.SiegeReward;
import com.ne.gs.services.mail.SystemMailService;
import com.ne.gs.services.player.PlayerService;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;

public class SourceSiege extends Siege<SourceLocation> {

    private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");

    private final AbyssPointsListener addAPListener = new AbyssPointsListener(this);

    public SourceSiege(SourceLocation siegeLocation) {
        super(siegeLocation);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onSiegeStart() {
        getSiegeLocation().setPreparation(false);
        getSiegeLocation().setVulnerable(true);
        getSiegeLocation().setUnderShield(true);
        broadcastState(getSiegeLocation());
        EventNotifier.GLOBAL.attach(addAPListener);
        deSpawnNpcs(getSiegeLocationId());
        spawnNpcsWithoutBoss(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.SIEGE);
	    ThreadPoolManager.getInstance().schedule(new SiegeBossSpawn(), 300000L);
    }

    @Override
    protected void onSiegeFinish() {
        EventNotifier.GLOBAL.detach(addAPListener);
        unregisterSiegeBossListeners();
        deSpawnNpcs(getSiegeLocationId());
        getSiegeLocation().setVulnerable(false);
        getSiegeLocation().setUnderShield(false);
        if (isBossKilled()) {
            onCapture();
            broadcastUpdate(getSiegeLocation(), getSiegeLocation().getTemplate().getNameId());
        } else {
            broadcastState(getSiegeLocation());
        }
        spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.PEACE);
        if (SiegeRace.BALAUR != getSiegeLocation().getRace()) {
            giveRewardsToPlayers(getSiegeCounter().getRaceCounter(getSiegeLocation().getRace()));
        }
        GDB.get(SiegeDAO.class).updateSiegeLocation(getSiegeLocation());
        updateTiamarantaRiftsStatus(false, false);
    }

    @Override
    public boolean isEndless() {
        return false;
    }

    @Override
    public void addAbyssPoints(Player player, int abysPoints) {
        getSiegeCounter().addAbyssPoints(player, abysPoints);
    }

    public void onCapture() {
        SiegeRaceCounter winner = getSiegeCounter().getWinnerRaceCounter();
        getSiegeLocation().setRace(winner.getSiegeRace());

        if (SiegeRace.BALAUR == winner.getSiegeRace()) {
            getSiegeLocation().setLegionId(0);
        } else {
            Integer topLegionId = winner.getWinnerLegionId();
            getSiegeLocation().setLegionId(topLegionId != null ? topLegionId : 0);
        }
    }

    protected void giveRewardsToPlayers(SiegeRaceCounter winnerDamage) {
        Map<Integer, Long> playerAbyssPoints = winnerDamage.getPlayerAbyssPoints();
        List<Integer> topPlayersIds = Lists.newArrayList(playerAbyssPoints.keySet());
        Map<Integer, String> playerNames = PlayerService.getPlayerNames(playerAbyssPoints.keySet());
        List<SiegeReward> playerRewards = getSiegeLocation().getReward();
        int i = 0;
        for (SiegeReward reward : playerRewards) {
            for (int id = 0; i < topPlayersIds.size() && id < reward.getTop(); ++i) {
                Integer playerId = topPlayersIds.get(i);
				Player player = World.getInstance().findPlayer(playerId);

	            if(player == null  || !player.isOnline() /*|| !getSiegeLocation().isInsideLocation(player)*/)
		            continue;

                ++id;

                String name = playerNames.get(playerId);
                int itemId = reward.getItemId();
                long count = reward.getCount() * SiegeConfig.SIEGE_MEDAL_RATE;

                if (LoggingConfig.LOG_SIEGE) {
                    log.info(String.format("[SIEGE] > [FORTRESS:%d] [RACE: %s] Player Reward to: %s] ITEM RETURN %d ITEM COUNT %d",
                        getSiegeLocationId(), getSiegeLocation().getRace(), name, itemId, count));
                }


                SystemMailService.getInstance().sendMail("SiegeService", name, "SiegeReward", "Medal", itemId, count, 0, LetterType.NORMAL);
            }
        }
    }

	private class SiegeBossSpawn implements Runnable {

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.SIEGE, AbyssNpcType.BOSS);
			initSiegeBoss();
		}
	}
}
