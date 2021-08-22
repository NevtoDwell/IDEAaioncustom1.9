/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.housing;

import java.util.Map;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.utils.collections.CopyOnWriteMap;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public class HouseInfo {

    private final House.HouseTemplate _template;
    private final House.Access _access;

    private HouseInfo(House.HouseTemplate template, House.Access access) {
        _template = template;
        _access = access;
    }

    @Nullable
    public House.HouseTemplate getTemplate() {
        return _template;
    }

    public boolean hasHouse() {
        return _template != null;
    }

    public int getId() {
        return hasHouse() ? getTemplate().getDoorId() + getTemplate().getMapId() : 0;
    }

    public int getHouseId() {
        return hasHouse() ? _template.getHouseId() : 0;
    }

    public int getAccessId() {
        return hasHouse() ? _access.id() : 0;
    }

    public boolean managerIs(int managerId) {
        return hasHouse() && Housing.findManagerId(getTemplate()) == managerId;
    }

    public boolean typeIs(@NotNull House.HouseType... types) {
        if (!hasHouse()) {
            return false;
        }

        for (House.HouseType t : types) {
            if (t.equals(getTemplate().getType())) {
                return true;
            }
        }

        return false;
    }

    public boolean mapIs(int mapId) {
        return hasHouse() && getTemplate().getMapId() == mapId;
    }

    // ------------------------------------------------------------------------

    private static final HouseInfo NONE = new HouseInfo(null, null);

    private static final Map<Integer, HouseInfo> _infos = CopyOnWriteMap.of();

    @NotNull
    public static HouseInfo of(@NotNull Player player) {
        return of(player.getObjectId());
    }

    @NotNull
    public static HouseInfo of(@NotNull Integer playerId) {
        HouseInfo info = _infos.get(playerId);
        return info != null ? info : NONE;
    }

    /**
     * Should be called only from sync context, e.g: actor
     *
     * @param playerId
     * @param house
     */
    static void update(@NotNull Integer playerId, @Nullable House house) {
        if (house == null) { // delete
            if (_infos.get(playerId) == null) {
                return;
            }
            _infos.remove(playerId);
        } else { // update
            _infos.put(playerId, new HouseInfo(house.getTemplate(), house.getAccess()));
        }
    }
}
