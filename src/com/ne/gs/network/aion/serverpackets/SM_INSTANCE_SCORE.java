/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import javolution.util.FastList;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.InstanceScoreType;
import com.ne.gs.model.instance.instancereward.DarkPoetaReward;
import com.ne.gs.model.instance.instancereward.DredgionReward;
import com.ne.gs.model.instance.instancereward.InstanceReward;
import com.ne.gs.model.instance.instancereward.PvPArenaReward;
import com.ne.gs.model.instance.playerreward.CruciblePlayerReward;
import com.ne.gs.model.instance.playerreward.DredgionPlayerReward;
import com.ne.gs.model.instance.playerreward.InstancePlayerReward;
import com.ne.gs.model.instance.playerreward.PvPArenaPlayerReward;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Dns, ginho1, nrg, xTz
 */
@SuppressWarnings("rawtypes")
public class SM_INSTANCE_SCORE extends AionServerPacket {

    private final int mapId;
    private int instanceTime;
    private final InstanceScoreType instanceScoreType;
    private final InstanceReward instanceReward;

    public SM_INSTANCE_SCORE(int instanceTime, InstanceReward instanceReward) {
        mapId = instanceReward.getMapId();
        this.instanceTime = instanceTime;
        this.instanceReward = instanceReward;
        instanceScoreType = instanceReward.getInstanceScoreType();
    }

    public SM_INSTANCE_SCORE(InstanceReward instanceReward, InstanceScoreType instanceScoreType) {
        mapId = instanceReward.getMapId();
        this.instanceReward = instanceReward;
        this.instanceScoreType = instanceScoreType;
    }

    public SM_INSTANCE_SCORE(InstanceReward instanceReward) {
        mapId = instanceReward.getMapId();
        this.instanceReward = instanceReward;
        instanceScoreType = instanceReward.getInstanceScoreType();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void writeImpl(AionConnection con) {
        int playerCount = 0;
        writeD(mapId);
        writeD(instanceTime);
        writeD(instanceScoreType.getId());
        switch (mapId) {
            case 300110000:
            case 300210000:
            case 300440000:
                fillTableWithGroup(Race.ELYOS);
                fillTableWithGroup(Race.ASMODIANS);
                DredgionReward dredgionReward = (DredgionReward) instanceReward;
                int elyosScore = dredgionReward.getPointsByRace(Race.ELYOS).intValue();
                int asmosScore = dredgionReward.getPointsByRace(Race.ASMODIANS).intValue();
                writeD(instanceScoreType.isEndProgress() ? (asmosScore > elyosScore ? 1 : 0) : 255);
                writeD(elyosScore);
                writeD(asmosScore);
                for (DredgionReward.DredgionRooms dredgionRoom : dredgionReward.getDredgionRooms()) {
                    writeC(dredgionRoom.getState());
                }
                break;
            case 300320000:
            case 300300000:
                for (CruciblePlayerReward playerReward : (FastList<CruciblePlayerReward>) instanceReward.getPlayersInside()) {
                    writeD(playerReward.getOwner()); // obj
                    writeD(playerReward.getPoints()); // points
                    writeD(playerReward.getPlayer().getPlayerClass().getClassId()); // unk
                    writeD(playerReward.getInsignia());
                    playerCount++;
                }
                if (playerCount < 6) {
                    writeB(new byte[16 * (6 - playerCount)]); // spaces
                }
                break;
            case 300040000:
                DarkPoetaReward dpr = (DarkPoetaReward) instanceReward;
                writeD(dpr.getPoints());
                writeD(dpr.getNpcKills());
                writeD(dpr.getGatherCollections()); // gathers
                writeD(dpr.getRank()); // 7 for none, 8 for F, 5 for D, 4 C, 3 B, 2 A, 1 S
                break;
            case 300350000:
            case 300360000:
            case 300420000:
            case 300430000:
                PvPArenaReward arenaReward = (PvPArenaReward) instanceReward;
                PvPArenaPlayerReward rewardedPlayer = arenaReward.getPlayerReward(con.getActivePlayer().getObjectId());
                boolean isRewarded = arenaReward.isRewarded();
                for (InstancePlayerReward reward : arenaReward.getPlayersInside()) {
                    PvPArenaPlayerReward playerReward = (PvPArenaPlayerReward) reward;
                    int points = playerReward.getPoints();
                    int rank = arenaReward.getRank(points);
                    Player player = playerReward.getPlayer();
                    writeD(player.getObjectId()); // obj
                    writeD(playerReward.getPvPKills()); // kills
                    writeD(isRewarded ? points + playerReward.getTimeBonus() : points); // points
                    writeD(player.getAbyssRank().getRank().getId()); // abyss rank
                    writeC(0); // unk
                    writeC(player.getPlayerClass().getClassId()); // class id
                    writeC(1); // unk
                    writeC(rank); // top position
                    writeD(playerReward.hasBoostMorale() ? 15 : 0);
                    writeD(arenaReward.getRankBonus(rank)); // rank bonus
                    writeD(isRewarded ? playerReward.getTimeBonus() : 0); // time bonus
                    writeD(0); // unk
                    writeD(0); // unk
                    writeS(player.getName(), 52); // playerName
                    playerCount++;
                }
                if (playerCount < 12) {
                    writeB(new byte[92 * (12 - playerCount)]); // spaces
                }
                if (isRewarded && arenaReward.canRewarded() && rewardedPlayer != null) {
                    writeD(rewardedPlayer.getAbyssPoints()); // abyss points
                    writeD(186000130); // 186000130
                    writeD(rewardedPlayer.getCrucibleInsignia()); // Crucible Insignia
                    writeD(186000137); // 186000137
                    writeD(rewardedPlayer.getCourageInsignia()); // Courage Insignia
                    if (arenaReward.canRewardOpportunityToken(rewardedPlayer)) {
                        writeD(186000165);
                        writeD(arenaReward.isSoloArena() ? 4 : 5);
                    } else {
                        writeD(0); // unk
                        writeD(0); // unk
                    }
                } else {
                    writeB(new byte[28]);
                }
                writeD(0); // unk
                writeD(0); // unk
                writeD(8);
                writeD(0); // unk
                writeD(arenaReward.getRound()); // round
                writeD(arenaReward.getCapPoints()); // cap points
                writeD(3); // unk
                writeD(0); // unk
                break;
        }
    }

    private void fillTableWithGroup(Race race) {
        int count = 0;
        DredgionReward dredgionReward = (DredgionReward) instanceReward;
        for (InstancePlayerReward playerReward : dredgionReward.getPlayersInsideByRace(race)) {
            DredgionPlayerReward dpr = (DredgionPlayerReward) playerReward;
            Player member = dpr.getPlayer();
            writeD(member.getObjectId()); // playerObjectId
            writeD(member.getAbyssRank().getRank().getId()); // playerRank
            writeD(dpr.getPvPKills()); // pvpKills
            writeD(dpr.getMonsterKills()); // monsterKills
            writeD(dpr.getZoneCaptured()); // captured
            writeD(dpr.getPoints()); // playerScore

            if (instanceScoreType.isEndProgress()) {
                boolean winner = race.equals(dredgionReward.getWinningRace());
                writeD((winner ? dredgionReward.getWinnerPoints() : dredgionReward.getLooserPoints()) + (int) (dpr.getPoints() * 1.6f)); // apBonus1
                writeD((winner ? dredgionReward.getWinnerPoints() : dredgionReward.getLooserPoints())); // apBonus2
            } else {
                writeB(new byte[8]);
            }

            writeC(member.getPlayerClass().getClassId()); // playerClass
            writeC(0); // unk
            writeS(member.getName(), 54); // playerName
            count++;
        }
        if (count < 6) {
            writeB(new byte[88 * (6 - count)]); // spaces
        }
    }

}
