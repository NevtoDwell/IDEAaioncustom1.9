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
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.database.dao.SiegeDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.siege.ArtifactLocation;
import com.ne.gs.model.siege.FortressLocation;
import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.templates.siegelocation.SiegeLegionReward;
import com.ne.gs.model.templates.siegelocation.SiegeReward;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.LegionService;
import com.ne.gs.services.SiegeService;
import com.ne.gs.services.mail.AbyssSiegeLevel;
import com.ne.gs.services.mail.MailFormatter;
import com.ne.gs.services.mail.SiegeResult;
import com.ne.gs.services.player.PlayerService;
import com.ne.gs.world.knownlist.Visitor;

/**
 * Object that controls siege of certain fortress. Siege object is not reusable.
 * New siege = new instance.
 * <p/>
 * TODO: Implement Balaur siege support
 *
 * @author SoulKeeper
 */
public class FortressSiege extends Siege<FortressLocation> {

    private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
    /**
     * AI name of siege boss npc. TODO: It's dirty hack, remove it in the future
     */
    private final AbyssPointsListener addAPListener = new AbyssPointsListener(this);

    public FortressSiege(FortressLocation fortress) {
        super(fortress);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSiegeStart() {

        // Mark fortress as vulnerable
        getSiegeLocation().setVulnerable(true);

        // Clear fortress from enemys
        broadcastState(getSiegeLocation());

        // Let the world know where the siege are
        getSiegeLocation().clearLocation();

        // Register abyss points listener
        // We should listen for abyss point callbacks that players are earning
        EventNotifier.GLOBAL.attach(addAPListener);

        // Spawn NPCs
        // respawn all NPCs so ppl cannot kill guards before siege
        deSpawnNpcs(getSiegeLocationId());
        spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.SIEGE);
        initSiegeBoss();
    }

    @Override
    public void onSiegeFinish() {

        // Unregister abyss points listener callback
        // We really don't need to add abyss points anymore
        EventNotifier.GLOBAL.detach(addAPListener);

        // Unregister siege boss listeners
        // cleanup :)
        unregisterSiegeBossListeners();

        SiegeService.getInstance().deSpawnNpcs(getSiegeLocationId());
        getSiegeLocation().setVulnerable(false);
        getSiegeLocation().setUnderShield(false);

        if (isBossKilled()) {
            onCapture();
            broadcastUpdate(getSiegeLocation());
        } else {
            broadcastState(getSiegeLocation());
        }
        SiegeService.getInstance().spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.PEACE);

        // Reward players and owning legion
        // If fortress was not captured by balaur
        if (SiegeRace.BALAUR != getSiegeLocation().getRace()) {
            giveRewardsToLegion();
            giveRewardsToPlayers(getSiegeCounter().getRaceCounter(getSiegeLocation().getRace()));
        }

        // Update outpost status
        // Certain fortresses are changing outpost ownership
        updateOutpostStatusByFortress(getSiegeLocation());

        // Update data in the GDB
        GDB.get(SiegeDAO.class).updateSiegeLocation(getSiegeLocation());

        getSiegeLocation().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                player.unsetInsideZoneType(ZoneType.SIEGE);
                if (isBossKilled() && (SiegeRace.getByRace(player.getRace()) == getSiegeLocation().getRace())) {
                    QuestEngine.getInstance().onKill(new QuestEnv(getBoss(), player, 0, 0));
                }
            }
        });
    }

    public void onCapture() {
        SiegeRaceCounter winner = getSiegeCounter().getWinnerRaceCounter();

        // Set new fortress and artifact owner race
        getSiegeLocation().setRace(winner.getSiegeRace());
        getArtifact().setRace(winner.getSiegeRace());

        // If new race is balaur
        if (SiegeRace.BALAUR == winner.getSiegeRace()) {
            getSiegeLocation().setLegionId(0);
            getArtifact().setLegionId(0);
        } else {
            Integer topLegionId = winner.getWinnerLegionId();
            getSiegeLocation().setLegionId(topLegionId != null ? topLegionId : 0);
            getArtifact().setLegionId(topLegionId != null ? topLegionId : 0);
        }
    }

    @Override
    public boolean isEndless() {
        return false;
    }

    @Override
    public void addAbyssPoints(Player player, int abysPoints) {
        getSiegeCounter().addAbyssPoints(player, abysPoints);
    }

    protected void giveRewardsToLegion() {
        // We do not give rewards if fortress was captured for first time
        if (isBossKilled()) {
            if (LoggingConfig.LOG_SIEGE) {
                log.info("[SIEGE] > [FORTRESS:" + getSiegeLocationId() + "] [RACE: " + getSiegeLocation().getRace() + "] [LEGION :" + getSiegeLocation().getLegionId() + "] Legion Reward not sending because fortress was captured(siege boss killed).");
            }
            return;
        }

        // Legion with id 0 = not exists?
        if (getSiegeLocation().getLegionId() == 0) {
            if (LoggingConfig.LOG_SIEGE) {
                log.info("[SIEGE] > [FORTRESS:" + getSiegeLocationId() + "] [RACE: " + getSiegeLocation().getRace() + "] [LEGION :" + getSiegeLocation().getLegionId() + "] Legion Reward not sending because fortress not owned by any legion.");
            }
            return;
        }

        List<SiegeLegionReward> legionRewards = getSiegeLocation().getLegionReward();
        int legionBGeneral = LegionService.getInstance().getLegionBGeneral(getSiegeLocation().getLegionId());
        if (legionBGeneral != 0) {
            PlayerCommonData BGeneral = GDB.get(PlayerDAO.class).loadPlayerCommonData(legionBGeneral);
            if (LoggingConfig.LOG_SIEGE) {
                log.info("[SIEGE] > [FORTRESS:" + getSiegeLocationId() + "] [RACE: " + getSiegeLocation().getRace() + "] Legion Reward in process... LegionId:"
                        + getSiegeLocation().getLegionId() + " General Name:" + BGeneral.getName());
            }
            if (legionRewards != null) {
                for (SiegeLegionReward medalsType : legionRewards) {
                    if (LoggingConfig.LOG_SIEGE) {
                        log.info("[SIEGE] > [Legion Reward to: " + BGeneral.getName() + "] ITEM RETURN " + medalsType.getItemId() + " ITEM COUNT "
                                + medalsType.getCount() * SiegeConfig.SIEGE_LEGION_MEDAL_RATE);
                    }
                    MailFormatter.sendAbyssRewardMail(getSiegeLocation(),
                            BGeneral, AbyssSiegeLevel.NONE, SiegeResult.PROTECT,
                            System.currentTimeMillis(), medalsType.getItemId(), medalsType.getCount() * SiegeConfig.SIEGE_MEDAL_RATE, 0);
//                    SystemMailService.getInstance().sendMail("SiegeService", BGeneral.getName(), "LegionReward", "Successful defence", medalsType.getItemId(),
//                            medalsType.getCount() * SiegeConfig.SIEGE_LEGION_MEDAL_RATE, 0L, LetterType.NORMAL);
                }
            }
        }
    }

    protected void giveRewardsToPlayers(SiegeRaceCounter winnerDamage) {

        // Get the map with playerId to siege reward
        Map<Integer, Long> playerAbyssPoints = winnerDamage.getPlayerAbyssPoints();
        List<Integer> topPlayersIds = Lists.newArrayList(playerAbyssPoints.keySet());
        Map<Integer, String> playerNames = PlayerService.getPlayerNames(playerAbyssPoints.keySet());
        SiegeResult result = isBossKilled() ? SiegeResult.OCCUPY : SiegeResult.DEFENDER;

        // Black Magic Here :)
        int i = 0;
        List<SiegeReward> playerRewards = getSiegeLocation().getReward();
        int rewardLevel = 0;
        for (SiegeReward topGrade : playerRewards) {
            AbyssSiegeLevel level = AbyssSiegeLevel.getLevelById(++rewardLevel);
            for (int rewardedPC = 0; i < topPlayersIds.size() && rewardedPC < topGrade.getTop(); ++i) {
                Integer playerId = topPlayersIds.get(i);
                PlayerCommonData pcd = GDB.get(PlayerDAO.class).loadPlayerCommonData(playerId);
                ++rewardedPC;
                if (LoggingConfig.LOG_SIEGE) {
                    log.info("[SIEGE]  > [FORTRESS:" + getSiegeLocationId() + "] [RACE: " + getSiegeLocation().getRace() + "] Player Reward to: "
                            + playerNames.get(playerId) + "] ITEM RETURN " + topGrade.getItemId() + " ITEM COUNT " + topGrade.getCount()
                            * SiegeConfig.SIEGE_MEDAL_RATE);
                }
                MailFormatter.sendAbyssRewardMail(getSiegeLocation(), pcd, level, result, System.currentTimeMillis(),
                        topGrade.getItemId(), topGrade.getCount() * SiegeConfig.SIEGE_MEDAL_RATE, 0);
//                SystemMailService.getInstance().sendMail("SiegeService", playerNames.get(playerId), "SiegeReward", "Medal", topGrade.getItemId(),
//                        topGrade.getCount() * SiegeConfig.SIEGE_MEDAL_RATE, 0L, LetterType.NORMAL);
            }
        }
        if (!isBossKilled()) {
            while (i < topPlayersIds.size()) {
                i++;
                Integer playerId = topPlayersIds.get(i);
                PlayerCommonData pcd = GDB.get(PlayerDAO.class).loadPlayerCommonData(playerId);
                //Send Announcement Mails without reward to the rest
                MailFormatter.sendAbyssRewardMail(getSiegeLocation(), pcd, AbyssSiegeLevel.NONE, SiegeResult.EMPTY, System.currentTimeMillis(), 0, 0, 0);
            }
        }
    }

    protected ArtifactLocation getArtifact() {
        return SiegeService.getInstance().getFortressArtifacts().get(getSiegeLocationId());
    }

    protected boolean hasArtifact() {
        return getArtifact() != null;
    }

}
