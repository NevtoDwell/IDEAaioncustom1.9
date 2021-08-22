/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.autogroup;

/**
 * @author xTz
 */
public class SearchInstance {

    private final long registrationTime = System.currentTimeMillis();
    private final byte instanceMaskId;
    private boolean isInvited = false;
    private final EntryRequestType ert;

    public SearchInstance(byte instanceMaskId, EntryRequestType ert) {
        this.instanceMaskId = instanceMaskId;
        this.ert = ert;
    }

    public byte getInstanceMaskId() {
        return instanceMaskId;
    }

    public int getRemainingTime() {
        return (int) (System.currentTimeMillis() - registrationTime) / 1000 * 256;
    }

    public boolean isInvited() {
        return isInvited;
    }

    public void setInvited(boolean isInvited) {
        this.isInvited = isInvited;
    }

    public EntryRequestType getEntryRequestType() {
        return ert;
    }

    public boolean isDredgion() {
        return instanceMaskId == 1 || instanceMaskId == 2 || instanceMaskId == 3;
    }
}
