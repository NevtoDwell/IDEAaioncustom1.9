/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network;

import java.sql.Timestamp;

/**
 * @author KID
 */
public class BannedMacEntry {

    private final String mac;
    private String details;
    private Timestamp timeEnd;

    public BannedMacEntry(String address, long newTime) {
        mac = address;
        updateTime(newTime);
    }

    public BannedMacEntry(String address, Timestamp time, String details) {
        mac = address;
        timeEnd = time;
        this.details = details;
    }

    public final void setDetails(String details) {
        this.details = details;
    }

    public final void updateTime(long newTime) {
        timeEnd = new Timestamp(newTime);
    }

    public final String getMac() {
        return mac;
    }

    public final Timestamp getTime() {
        return timeEnd;
    }

    public final boolean isActive() {
        return timeEnd != null && timeEnd.getTime() > System.currentTimeMillis();
    }

    public final boolean isActiveTill(long time) {
        return timeEnd != null && timeEnd.getTime() > time;
    }

    public final String getDetails() {
        return details;
    }
}
