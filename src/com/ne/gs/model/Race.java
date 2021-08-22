/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 * Basic enum with races.<br>
 * I believe that NPCs will have their own races, so it's quite comfortable to have it in the same place
 *
 * @author SoulKeeper
 */

@XmlEnum
public enum Race {
    /**
     * Playable races
     */
    ELYOS(0, DescId.of(480480), "Элийцы"),
    ASMODIANS(1, DescId.of(480481), "Асмодиане"),

    /**
     * Npc races
     */
    LYCAN(2),
    CONSTRUCT(3),
    CARRIER(4),
    DRAKAN(5),
    LIZARDMAN(6),
    TELEPORTER(7),
    NAGA(8),
    BROWNIE(9),
    KRALL(10),
    SHULACK(11),
    BARRIER(12),
    PC_LIGHT_CASTLE_DOOR(13),
    PC_DARK_CASTLE_DOOR(14),
    DRAGON_CASTLE_DOOR(15),
    GCHIEF_LIGHT(16),
    GCHIEF_DARK(17),
    DRAGON(18),
    OUTSIDER(19),
    RATMAN(20),
    DEMIHUMANOID(21),
    UNDEAD(22),
    BEAST(23),
    MAGICALMONSTER(24),
    ELEMENTAL(25),
    LIVINGWATER(28),

    /**
     * Special races
     */
    NONE(26),
    PC_ALL(27),
    DEFORM(28),

    // 2.6
    NEUT(29),
    // 2.7 -- NOT SURE !!!
    GHENCHMAN_LIGHT(30),
    GHENCHMAN_DARK(31),

    EVENT_TOWER_DARK(32),
    EVENT_TOWER_LIGHT(33),
    GOBLIN(34),
    TRICODARK(35),
    NPC(36);

    private final int raceId;
    private final DescId _descId;
    private final String rusname;

    private Race(int raceId) {
        this(raceId, null, "");
    }

    private Race(int raceId, DescId descId, String rusname) {
        this.raceId = raceId;
        this._descId = descId;
        this.rusname = rusname;
    }

    public int getRaceId() {
        return raceId;
    }

    public String getRusname() {
        return rusname;
    }

    public DescId getRaceDescriptionId() {
        if (_descId == null) {
            throw new RuntimeException("Race name DescriptionId is unknown for race" + this);
        }

        return _descId;
    }
}
