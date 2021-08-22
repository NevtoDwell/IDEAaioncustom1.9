/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

import java.util.List;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.siegelocation.SiegeLocationTemplate;
import com.ne.gs.model.templates.siegelocation.SiegeReward;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.zone.ZoneInstance;

public class SourceLocation extends SiegeLocation {

    protected List<SiegeReward> siegeRewards;
    private boolean status;

    public SourceLocation() {
    }

    public SourceLocation(SiegeLocationTemplate template) {
        super(template);
        siegeRewards = template.getSiegeRewards() != null ? template.getSiegeRewards() : null;
    }

    public List<SiegeReward> getReward() {
        return siegeRewards;
    }

    public boolean isPreparations() {
        return status;
    }

    public void setPreparation(boolean status) {
        this.status = status;
    }

    @Override
    public void onEnterZone(Creature creature, ZoneInstance zone) {
        super.onEnterZone(creature, zone);
        if (isVulnerable()) {
            creature.setInsideZoneType(ZoneType.SIEGE);
        }
    }

    @Override
    public void onLeaveZone(Creature creature, ZoneInstance zone) {
        super.onLeaveZone(creature, zone);
        if (isVulnerable()) {
            creature.unsetInsideZoneType(ZoneType.SIEGE);
        }
    }

    public WorldPosition getEntryPosition() {
        WorldPosition pos = new WorldPosition();
        pos.setMapId(getWorldId());
        switch (getLocationId()) {
            case 4011:
                pos.setXYZH(332.14316F, 854.36053F, 313.98001F, (byte) 77);
                break;
            case 4021:
                pos.setXYZH(2353.9065F, 378.19449F, 237.8031F, (byte) 113);
                break;
            case 4031:
                pos.setXYZH(879.23627F, 2712.4644F, 254.25073F, (byte) 85);
                break;
            case 4041:
                pos.setXYZH(2901.2354F, 2365.0383F, 339.14691F, (byte) 39);
        }

        return pos;
    }

    @Override
    public void clearLocation() {
        for (Player player : getPlayers().values()) {
            WorldPosition pos = getEntryPosition();
            World.getInstance().updatePosition(player, pos.getX(), pos.getY(), pos.getZ(), player.getHeading());
            PacketSendUtility.broadcastPacketAndReceive(player, new SM_MOVE(player));
        }
    }
}
