/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import com.ne.gs.model.gameobjects.PersistentState;

/**
 * @author ATracer
 */
public class PlayerSettings {

    private PersistentState persistentState;

    private byte[] uiSettings;
    private byte[] shortcuts;
    private int deny = 0;
    private int display = 0;

    public PlayerSettings() {

    }

    public PlayerSettings(byte[] uiSettings, byte[] shortcuts, int deny, int display) {
        this.uiSettings = uiSettings;
        this.shortcuts = shortcuts;
        this.deny = deny;
        this.display = display;
    }

    /**
     * @return the persistentState
     */
    public PersistentState getPersistentState() {
        return persistentState;
    }

    /**
     * @param persistentState
     *     the persistentState to set
     */
    public void setPersistentState(PersistentState persistentState) {
        this.persistentState = persistentState;
    }

    /**
     * @return the uiSettings
     */
    public byte[] getUiSettings() {
        return uiSettings;
    }

    /**
     * @param uiSettings
     *     the uiSettings to set
     */
    public void setUiSettings(byte[] uiSettings) {
        this.uiSettings = uiSettings;
        persistentState = PersistentState.UPDATE_REQUIRED;
    }

    /**
     * @return the shortcuts
     */
    public byte[] getShortcuts() {
        return shortcuts;
    }

    /**
     * @param shortcuts
     *     the shortcuts to set
     */
    public void setShortcuts(byte[] shortcuts) {
        this.shortcuts = shortcuts;
        persistentState = PersistentState.UPDATE_REQUIRED;
    }

    /**
     * @return the display
     */
    public int getDisplay() {
        return display;
    }

    /**
     * @param display
     *     the display to set
     */
    public void setDisplay(int display) {
        this.display = display;
        persistentState = PersistentState.UPDATE_REQUIRED;
    }

    /**
     * @return the deny
     */
    public int getDeny() {
        return deny;
    }

    /**
     * @param deny
     *     the deny to set
     */
    public void setDeny(int deny) {
        this.deny = deny;
        persistentState = PersistentState.UPDATE_REQUIRED;
    }

    public boolean isInDeniedStatus(DeniedStatus deny) {
        int isDeniedStatus = this.deny & deny.getId();

        return isDeniedStatus == deny.getId();
    }
}
