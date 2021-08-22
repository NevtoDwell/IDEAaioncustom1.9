/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team.legion;

/**
 * @author MrPoke
 */
public enum LegionPermissionsMask {

    EDIT(0x200),
    INVITE(0x8),
    KICK(0x10),
    WH_WITHDRAWAL(0x4),
    WH_DEPOSIT(0x1000),
    ARTIFACT(0x400),
    GUARDIAN_STONE(0x800);

    private final int rank;

    private LegionPermissionsMask(int rank) {
        this.rank = rank;
    }

    public boolean can(int permission) {
        return (rank & permission) != 0;
    }
}
