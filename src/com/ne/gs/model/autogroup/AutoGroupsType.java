/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.autogroup;

import java.util.List;

import com.ne.gs.dataholders.DataManager;

/**
 * @author xTz
 */
public enum AutoGroupsType {

    BARANATH_DREDGION((byte) 1, 600000, 12),
    CHANTRA_DREDGION((byte) 2, 600000, 12),
    TERATH_DREDGION((byte) 3, 600000, 12),
    ELYOS_FIRE_TEMPLE((byte) 4, 300000, 6),
    NOCHSANA_TRAINING_CAMP((byte) 5, 600000, 6),
    DARK_POETA((byte) 6, 1200000, 6),
    STEEL_RAKE((byte) 7, 1200000, 6),
    UDAS_TEMPLE((byte) 8, 600000, 6),
    LOWER_UDAS_TEMPLE((byte) 9, 600000, 6),
    EMPYREAN_CRUCIBLE((byte) 11, 600000, 6),
    ASMODIANS_FIRE_TEMPLE((byte) 14, 300000, 6),
    ARENA_OF_CHAOS_1((byte) 21, 110000, 10, 1),
    ARENA_OF_CHAOS_2((byte) 22, 110000, 10, 2),
    ARENA_OF_CHAOS_3((byte) 23, 110000, 10, 3),
    ARENA_OF_DISCIPLINE_1((byte) 24, 110000, 2, 1),
    ARENA_OF_DISCIPLINE_2((byte) 25, 110000, 2, 2),
    ARENA_OF_DISCIPLINE_3((byte) 26, 110000, 2, 3),
    CHAOS_TRAINING_GROUNDS_1((byte) 27, 110000, 10, 1),
    CHAOS_TRAINING_GROUNDS_2((byte) 28, 110000, 10, 2),
    CHAOS_TRAINING_GROUNDS_3((byte) 29, 110000, 10, 3),
    DISCIPLINE_TRAINING_GROUNDS_1((byte) 30, 110000, 2, 1),
    DISCIPLINE_TRAINING_GROUNDS_2((byte) 31, 110000, 2, 2),
    DISCIPLINE_TRAINING_GROUNDS_3((byte) 32, 110000, 2, 3),
    CHAOS_TRAINING_GROUNDS((byte) 99, 0, 10),
    DISCIPLINE_TRAINING_GROUNDS((byte) 100, 0, 2);

    private final byte instanceMaskId;
    private final int time;
    private final int playerSize;
    private final int difficultId;
    private final AutoGroup template;

    private AutoGroupsType(byte instanceMaskId, int time, int playerSize, int difficultId) {
        this.instanceMaskId = instanceMaskId;
        this.time = time;
        this.playerSize = playerSize;
        this.difficultId = difficultId;
        template = DataManager.AUTO_GROUP.getTemplateByInstaceMaskId(instanceMaskId);
    }

    private AutoGroupsType(byte instanceMaskId, int time, int playerSize) {
        this(instanceMaskId, time, playerSize, 0);
    }

    public int getInstanceMapId() {
        return template.getInstanceId();
    }

    public int getPlayerSize() {
        return playerSize;
    }

    public byte getInstanceMaskId() {
        return instanceMaskId;
    }

    public int getNameId() {
        return template.getNameId();
    }

    public int getTittleId() {
        return template.getTitleId();
    }

    public int getTime() {
        return time;
    }

    public int getMinLevel() {
        return template.getMinLvl();
    }

    public int getMaxLevel() {
        return template.getMaxLvl();
    }

    public boolean hasRegisterGroup() {
        return template.hasRegisterGroup();
    }

    public boolean hasRegisterQuick() {
        return template.hasRegisterQuick();
    }

    public boolean hasRegisterNew() {
        return template.hasRegisterNew();
    }

    public boolean containNpcId(int npcId) {
        return template.getNpcIds().contains(npcId);
    }

    public List<Integer> getNpcIds() {
        return template.getNpcIds();
    }

    public static AutoGroupsType getAutoGroupByInstanceMaskId(byte instanceMaskId) {
        for (AutoGroupsType autoGroupsType : values()) {
            if (autoGroupsType.getInstanceMaskId() == instanceMaskId) {
                return autoGroupsType;
            }
        }
        return null;
    }

    public static AutoGroupsType getAutoGroup(int level, int npcId) {
        for (AutoGroupsType agt : values()) {
            if (agt.hasLevelPermit(level) && agt.containNpcId(npcId)) {
                return agt;
            }
        }
        return null;
    }

    public static AutoGroupsType getAutoGroup(int npcId) {
        for (AutoGroupsType agt : values()) {
            if (agt.containNpcId(npcId)) {
                return agt;
            }
        }
        return null;
    }

    public boolean isPvPSoloArena() {
        switch (this) {
            case ARENA_OF_DISCIPLINE_1:
            case ARENA_OF_DISCIPLINE_2:
            case ARENA_OF_DISCIPLINE_3:
                return true;
        }
        return false;
    }

    public boolean isTrainigPvPSoloArena() {
        switch (this) {
            case DISCIPLINE_TRAINING_GROUNDS_1:
            case DISCIPLINE_TRAINING_GROUNDS_2:
            case DISCIPLINE_TRAINING_GROUNDS_3:
                return true;
        }
        return false;
    }

    public boolean isPvPFFAArena() {
        switch (this) {
            case ARENA_OF_CHAOS_1:
            case ARENA_OF_CHAOS_2:
            case ARENA_OF_CHAOS_3:
                return true;
        }
        return false;
    }

    public boolean isTrainigPvPFFAArena() {
        switch (this) {
            case CHAOS_TRAINING_GROUNDS_1:
            case CHAOS_TRAINING_GROUNDS_2:
            case CHAOS_TRAINING_GROUNDS_3:
                return true;
        }
        return false;
    }

    public boolean isDredgion() {
        switch (this) {
            case TERATH_DREDGION:
            case CHANTRA_DREDGION:
            case BARANATH_DREDGION:
                return true;
        }
        return false;
    }

    public boolean isPvpArena() {
        return isTrainigPvPFFAArena() || isPvPFFAArena() || isTrainigPvPSoloArena() || isPvPSoloArena();
    }

    public boolean hasLevelPermit(int level) {
        return level >= getMinLevel() && level <= getMaxLevel();
    }

    public int getDifficultId() {
        return difficultId;
    }
}
