/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion;

/**
 * @author ATracer
 */
public enum InventoryPacketType {

    WAREHOUSE(false, false, false),
    INVENTORY(true, false, false),
    MAIL_REPURCHASE(false, true, false),
    PRIVATE_STORE(false, false, true),
    WEAPON_SWITCH(true,
        false, false, true);

    private final boolean isInventory;
    private final boolean isMailOrRepurchase;
    private final boolean isPrivateStore;
    private final boolean isWeaponSwitch;

    private InventoryPacketType(boolean isInventory, boolean isMail, boolean isPrivateStore) {
        this(isInventory, isMail, isPrivateStore, false);
    }

    private InventoryPacketType(boolean isInventory, boolean isMail, boolean isPrivateStore, boolean isWeaponSwitch) {
        this.isInventory = isInventory;
        isMailOrRepurchase = isMail;
        this.isPrivateStore = isPrivateStore;
        this.isWeaponSwitch = isWeaponSwitch;
    }

    public final boolean isInventory() {
        return isInventory;
    }

    public final boolean isMail() {
        return isMailOrRepurchase;
    }

    public final boolean isRepurchase() {
        return isMailOrRepurchase;
    }

    public final boolean isPrivateStore() {
        return isPrivateStore;
    }

    public final boolean isWeaponSwitch() {
        return isWeaponSwitch;
    }

}
