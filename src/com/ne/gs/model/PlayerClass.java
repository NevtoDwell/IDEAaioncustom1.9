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
 * This enum represent class that a player may belong to.
 *
 * @author Luno
 */
@XmlEnum
public enum PlayerClass {
    WARRIOR(0, true),
    GLADIATOR(1),
    TEMPLAR(2),
    SCOUT(3, true),
    ASSASSIN(4),
    RANGER(5),
    MAGE(6, true),
    SORCERER(7),
    SPIRIT_MASTER(8),
    PRIEST(9, true),
    CLERIC(10),
    CHANTER(11),
    ALL(12);

    /**
     * This id is used on client side
     */
    private final byte classId;

    /**
     * This is the mask for this class id, used with bitwise AND in arguments that contain more than one possible class
     */
    private final int idMask;

    /**
     * Tells whether player can create new character with this class
     */
    private final boolean startingClass;

    PlayerClass(int classId) {
        this(classId, false);
    }

    PlayerClass(int classId, boolean startingClass) {
        this.classId = (byte) classId;
        this.startingClass = startingClass;
        idMask = (int) Math.pow(2, classId);
    }

    /**
     * Returns client-side id for this PlayerClass
     *
     * @return classID
     */
    public byte getClassId() {
        return classId;
    }

    /**
     * Returns <tt>PlayerClass</tt> object correlating with given classId.
     *
     * @param classId
     *     - id of player class
     *
     * @return PlayerClass objects that matches the given classId. If there isn't any objects that matches given id, then <b>IllegalArgumentException</b> is
     *         being thrown.
     */
    public static PlayerClass getPlayerClassById(byte classId) {
        for (PlayerClass pc : values()) {
            if (pc.getClassId() == classId) {
                return pc;
            }
        }

        throw new IllegalArgumentException("There is no player class with id " + classId);
    }

    /**
     * @return true if this is one of starting classes ( player can create char with this class )
     */
    public boolean isStartingClass() {
        return startingClass;
    }

    /**
     * @param pc
     *
     * @return starting class for second class
     */
    public static PlayerClass getStartingClassFor(PlayerClass pc) {
        switch (pc) {
            case ASSASSIN:
            case RANGER:
                return SCOUT;
            case GLADIATOR:
            case TEMPLAR:
                return WARRIOR;
            case CHANTER:
            case CLERIC:
                return PRIEST;
            case SORCERER:
            case SPIRIT_MASTER:
                return MAGE;
            case SCOUT:
            case WARRIOR:
            case PRIEST:
            case MAGE:
                return pc;
            default:
                throw new IllegalArgumentException("Given player class is starting class: " + pc);
        }
    }

    public static PlayerClass getPlayerClassByString(String fieldName) {
        for (PlayerClass pc : values()) {
            if (pc.toString().equals(fieldName)) {
                return pc;
            }
        }
        return null;
    }

    public int getMask() {
        return idMask;
    }
}
