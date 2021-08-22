/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.movement;

/**
 * @author Mr. Poke
 */
public final class MovementMask {

    public static final byte IMMEDIATE = 0;
    public static final byte GLIDE = (byte) 0x04;
    public static final byte FALL = (byte) 0x08;
    public static final byte VEHICLE = (byte) 0x10;
    public static final byte MOUSE = (byte) 0x20;
    public static final byte STARTMOVE = (byte) 0xC0;
    public static final byte NPC_WALK_SLOW = -22;
    public static final byte NPC_WALK_FAST = -24;
    public static final byte NPC_RUN_SLOW = -28;
    public static final byte NPC_RUN_FAST = -30;
    public static final byte NPC_STARTMOVE = -32;
}
