/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

import java.io.Serializable;

/**
 * @author zdead
 */
public class AbyssRankingResult implements Serializable {

    private static final long serialVersionUID = -1413227745923480293L;

    private String playerName;
    private int playerAbyssRank;
    private final int oldRankPos;
    private int rankPos;
    private int ap;
    private int title;
    private PlayerClass playerClass;
    private int playerLevel;
    private int playerId;

    private final String legionName;
    private long cp;
    private int legionId;
    private int legionLevel;
    private int legionMembers;

    public AbyssRankingResult(String playerName, int playerAbyssRank, int playerId, int ap, int title,
                              PlayerClass playerClass, int playerLevel, String legionName, int oldRankPos, int rankPos) {
        this.playerName = playerName;
        this.playerAbyssRank = playerAbyssRank;
        this.playerId = playerId;
        this.ap = ap;
        this.title = title;
        this.playerClass = playerClass;
        this.playerLevel = playerLevel;
        this.legionName = legionName;
        this.oldRankPos = oldRankPos;
        this.rankPos = rankPos;
    }

    public AbyssRankingResult(long cp, String legionName, int legionId, int legionLevel, int legionMembers, int oldRankPos,
                              int rankPos) {
        this.oldRankPos = oldRankPos;
        this.rankPos = rankPos;
        this.cp = cp;
        this.legionName = legionName;
        this.legionId = legionId;
        this.legionLevel = legionLevel;
        this.legionMembers = legionMembers;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getPlayerAbyssRank() {
        return playerAbyssRank;
    }

    /**
     * @return the oldRankPos
     */
    public int getOldRankPos() {
        return oldRankPos;
    }

    public int getRankPos() {
        return rankPos;
    }

    public int getPlayerAP() {
        return ap;
    }

    public int getPlayerTitle() {
        return title;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public String getLegionName() {
        return legionName;
    }

    public long getLegionCP() {
        return cp;
    }

    public int getLegionId() {
        return legionId;
    }

    public int getLegionLevel() {
        return legionLevel;
    }

    public int getLegionMembers() {
        return legionMembers;
    }

    public void setRankPos(int rankPos) {
        this.rankPos = rankPos;
    }
}
